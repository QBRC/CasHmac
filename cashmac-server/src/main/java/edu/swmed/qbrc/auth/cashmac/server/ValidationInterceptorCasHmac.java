package edu.swmed.qbrc.auth.cashmac.server;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import java.lang.Class;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import edu.swmed.qbrc.auth.cashmac.server.dao.UserDao;
import edu.swmed.qbrc.auth.cashmac.server.data.Role;
import edu.swmed.qbrc.auth.cashmac.server.data.User;
import edu.swmed.qbrc.auth.cashmac.server.guice.GuiceModule;
import edu.swmed.qbrc.auth.cashmac.shared.util.Securable;
import edu.swmed.qbrc.auth.cashmac.shared.util.HMACUtils;

@Provider
@ServerInterceptor
public class ValidationInterceptorCasHmac implements PreProcessInterceptor, AcceptedByMethod {

  @Context
  private HttpServletRequest servletRequest;
  @Context
  private HttpServletResponse servletResponse;
  @Context
  private ServletConfig servletConfig;

  @Inject UserDao userDao;
  
  private String casServerLoginUrl;
  private String serverName; 
  
  private static final String TICKET_PARAM_NAME = "ticket";
  private static final String SERVICE_PARAM_NAME = "service";
  
  private boolean isInjected = false;


  /**
   *  Only authenticate requests to REST functions that include the @Securable annotation
   */
  public boolean accept(@SuppressWarnings("rawtypes") Class clazz, Method method) {
	  return method.isAnnotationPresent(Securable.class);
  }

  /**
   * While web.xml contains most of the CAS configuration, we need to implement our own code to
   * replace AuthenticationFilter since we don't want to authenticate all requests with CAS.
   */
  public ServerResponse preProcess(HttpRequest httpRequest, ResourceMethod resourceMethod)
		  throws Failure, WebApplicationException {
	
	  
	final HttpServletRequest request = (HttpServletRequest) servletRequest;
	final HttpServletResponse response = (HttpServletResponse) servletResponse;
	final HttpSession session = request.getSession(false);

	/* Sets up the injector and inject self, if not already done.
	 * We do this within the interceptor implementation since it appears to be the only
	 * place where the "servletConfig" variable is in scope.  We need to pass servletConfig
	 * to the Guice Module in order for it to be available to other objects (DAOs).
	 */
	if (! isInjected) {
		System.out.println("Initializing Guice Injector.");
		Injector injector = Guice.createInjector(new GuiceModule(servletConfig));
		injector.injectMembers(this);
		isInjected = true;
	}

	// Get roles required by REST service
	Securable securable =  resourceMethod.getMethod().getAnnotation(Securable.class);
	String roles = securable.value();

	// First, check for HMAC authentication
	String hmacSignature = request.getHeader("Signature");
	String dateSignature = request.getHeader("Date");
	String clientId = request.getHeader("ClientId");
	if (hmacSignature != null && dateSignature != null && clientId != null) {
		try {
			
			// Check for 5 minute window for date
			Date clientDate;
			long serverDate = System.currentTimeMillis();
			Date serverDateStart = new Date(serverDate - 300000);
			Date serverDateEnd = new Date(serverDate + 300000);
			try {
				clientDate = new Date(Long.valueOf(dateSignature));
			} catch (Exception e) {
				return (ServerResponse)Response.status(Status.FORBIDDEN).entity("Invalid Client Date").build();
			}
			if (! (clientDate.after(serverDateStart) && clientDate.before(serverDateEnd))) {
				return (ServerResponse)Response.status(Status.FORBIDDEN).entity("Client Date Not Within Acceptable Range").build();
			}
			
			// Recreate string to sign
			String toSign = HMACUtils.createSignature(httpRequest, dateSignature);
			
			// Look up user
			try {
				User user = userDao.load(clientId);
				if (user != null) {
					// Create signature, and check it against the original signature
					String hmac = HMACUtils.calculateRFC2104HMAC(toSign, user.getSecret());
					if (hmac.equals(hmacSignature)) {

						// Allow any authenticated user if the role is blank.
						if (roles.equals("")) {
							return null;
						}
						
						// If the annotation includes a role or roles, check to see if the user has the role
						else if (checkRoles(user.getRoles(), roles)) {
							return null;
						}
						
						// Error for users without a role valid for the method.
						else {
							return (ServerResponse)Response.status(Status.FORBIDDEN).entity("User not in possession of necessary role.").build();
						}
						
					} else {
						return (ServerResponse)Response.status(Status.FORBIDDEN).entity("Invalid HMAC").build();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return (ServerResponse)Response.status(Status.FORBIDDEN).entity("Error Looking up Client by Id").build();
			}
			
		} catch (Exception e) {
	    	return (ServerResponse)Response.status(Status.FORBIDDEN).entity("Error calculating HMAC: " + e.getMessage()).build();
		}
	}
	  
	// Get context parameters for CAS server login URL and service URL
	casServerLoginUrl = servletConfig.getServletContext().getInitParameter("edu.swmed.qbrc.auth.cashmac.cas.serverLoginUrl");
	serverName = servletConfig.getServletContext().getInitParameter("edu.swmed.qbrc.auth.cashmac.cas.serviceName");

	final Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;
	if (assertion != null) {
		return null;
	}

	final String serviceUrl =  CommonUtils.constructServiceUrl(request, response, null, this.serverName, TICKET_PARAM_NAME, true);
    final String ticket = CommonUtils.safeGetParameter(request, TICKET_PARAM_NAME);

    if (CommonUtils.isNotBlank(ticket)) {
        return null;
    }
   
    final String modifiedServiceUrl = serviceUrl;
    final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl, SERVICE_PARAM_NAME, modifiedServiceUrl, false, false);

    try {
    	return (ServerResponse)Response.status(Status.TEMPORARY_REDIRECT).location(new URI(urlToRedirectTo)).build();
	} catch (URISyntaxException e) {
    	return (ServerResponse)Response.status(Status.FORBIDDEN).entity("Invalid CAS Login URL: " + urlToRedirectTo).build();
	}
    
  }
  
  private Boolean checkRoles(List<Role> roles, String annotatedRoles) {
	  
	  List<String> annotatedRolesList = Arrays.asList(annotatedRoles.split(","));
	  
	  for (String annotatedRole : annotatedRolesList) {
		  for (Role role : roles){
			  if (role.getRole().equals(annotatedRole)) {
				  return true;
			  }
		  }
	  }
	  
	  return false;
  }


}