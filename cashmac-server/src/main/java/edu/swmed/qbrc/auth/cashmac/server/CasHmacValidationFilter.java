package edu.swmed.qbrc.auth.cashmac.server;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import javax.annotation.Priority;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;
import com.google.inject.Guice;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.dao.UserDao;
import edu.swmed.qbrc.auth.cashmac.server.data.User;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;
import edu.swmed.qbrc.auth.cashmac.server.guice.GuiceModule;
import edu.swmed.qbrc.auth.cashmac.server.guice.MainGuiceModule;
import edu.swmed.qbrc.auth.cashmac.shared.annotations.NoCasAuth;
import edu.swmed.qbrc.auth.cashmac.shared.util.HMACUtils;

/**
 * This is the default HMAC/CAS validation filter.  If the @NoCasAuth annotation is
 * applied to a resource method, then the CasHmacValidationFilterNoCas takes priority
 * and CAS is not allowed.  This filter, however, allows both CAS and HMAC authentication.
 * @author JYODE1
 *
 */
@Priority(0)
public class CasHmacValidationFilter implements ContainerRequestFilter {

	@Context
	private HttpServletRequest servletRequest;
	@Context
	private HttpServletResponse servletResponse;

	@Inject UserDao userDao;

	private String casServerLoginUrl;
	private String serverName; 
	private boolean isInjected = false;

	private static final Logger log = Logger.getLogger(CasHmacValidationFilter.class);
	private static final String TICKET_PARAM_NAME = "ticket";
	private static final String SERVICE_PARAM_NAME = "service";
	//private static final String TICKET_PARAM_NAME = "SAMLart";
	//private static final String SERVICE_PARAM_NAME = "TARGET";
	
	@Override
	public void filter(ContainerRequestContext context) throws IOException {

		/* 
		 * Sets up the injector and inject self, if not already done.
		 */
		if (! isInjected) {
			log.info("Initializing Guice Injector.");
			GuiceModule guiceModule = Guice.createInjector(MainGuiceModule.getModule()).getInstance(GuiceModule.class);
			Guice.createInjector(guiceModule).injectMembers(this);
			isInjected = true;
		}
		
		// First, check for HMAC authentication
		String hmacSignature = context.getHeaderString("Signature");
		String dateSignature = context.getHeaderString("Date");
		String clientId = context.getHeaderString("ClientId");
		if (hmacSignature != null && dateSignature != null && clientId != null) {
			try {
				
				// Check for 5 minute window for date
				Date clientDate = null;
				long serverDate = System.currentTimeMillis();
				Date serverDateStart = new Date(serverDate - 300000);
				Date serverDateEnd = new Date(serverDate + 300000);
				try {
					clientDate = new Date(Long.valueOf(dateSignature));
				} catch (Exception e) {
					context.abortWith(Response.status(Status.FORBIDDEN).entity("Invalid Client Date").build());
				}
				if (! (clientDate != null && clientDate.after(serverDateStart) && clientDate.before(serverDateEnd))) {
					context.abortWith(Response.status(Status.FORBIDDEN).entity("Client Date Not Within Acceptable Range").build());
				}
				
				// Recreate string to sign
				String toSign = HMACUtils.createSignature(context, dateSignature);
				
				// Look up user
				try {
					User user = userDao.load(clientId);
					if (user != null) {
						// Create signature, and check it against the original signature
						String hmac = HMACUtils.calculateRFC2104HMAC(toSign, user.getSecret());
						if (hmac.equals(hmacSignature)) {

							// Check to see if the user has the role (allow any authenticated user if no role specified).
							CasHmacRequestFilter.getRequest().setAttribute("user", user);
							context.setSecurityContext(new CasHmacSecurityContext(user));
							
							return;
							
						} else {
							context.abortWith(Response.status(Status.FORBIDDEN).entity("Invalid HMAC").build());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					context.abortWith(Response.status(Status.FORBIDDEN).entity("Error Looking up Client by Id").build());
				}
				
			} catch (Exception e) {
				context.abortWith(Response.status(Status.FORBIDDEN).entity("Error calculating HMAC: " + e.getMessage()).build());
			}
		}
		
		// Search for @NoCasAuth annotation on resource method.
		PostMatchContainerRequestContext ctx = (PostMatchContainerRequestContext)context;
		if (! ctx.getResourceMethod().getMethod().isAnnotationPresent(NoCasAuth.class)) {
		
			// Get context parameters for CAS server login URL and service URL
			casServerLoginUrl = CasHmacRequestFilter.getConfig().get("edu.swmed.qbrc.auth.cashmac.cas.serverLoginUrl");
			serverName = CasHmacRequestFilter.getConfig().get("edu.swmed.qbrc.auth.cashmac.cas.serviceName");

			final HttpSession session = CasHmacRequestFilter.getSession();
			final Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;
			if (assertion != null && assertion.getPrincipal() != null) {
				log.trace("Assertion Principal: " + assertion.getPrincipal().getName());

				// Check to see if the user has the role (allow any authenticated user if no role specified).
				CasHmacRequestFilter.getRequest().setAttribute("user", new User(assertion.getPrincipal().getName(), "", ""));
				context.setSecurityContext(new CasHmacSecurityContextCAS(assertion.getPrincipal()));
				
				log.trace("Assertion Found. Allowing Request.");
				return;
			}
	
			// If we have a valid ticket, allow the user in.
			/*
			final String ticket = CommonUtils.safeGetParameter(CasHmacRequestFilter.getRequest(), TICKET_PARAM_NAME);
			if (CommonUtils.isNotBlank(ticket)) {
				log.trace("Ticket Found. Allowing Request.");
			    return;
			}
			*/

			// Redirect to CAS Login
			final String serviceUrl = CommonUtils.constructServiceUrl(CasHmacRequestFilter.getRequest(), CasHmacRequestFilter.getResponse(), null, this.serverName, TICKET_PARAM_NAME, true);
			final String modifiedServiceUrl = serviceUrl;
			final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl, SERVICE_PARAM_NAME, modifiedServiceUrl, false, false);
			try {
				context.abortWith(Response.status(Status.TEMPORARY_REDIRECT).location(new URI(urlToRedirectTo)).build());
			} catch (URISyntaxException e) {
				context.abortWith(Response.status(Status.FORBIDDEN).entity("Invalid CAS Login URL: " + urlToRedirectTo).build());
			}
		}
		
		else {
			// Don't allow CAS if the "@NoCasAuth" annotation is present on the RESTful method.
			context.abortWith(Response.status(Status.FORBIDDEN).entity("No HMAC authentication information was supplied, and CAS authentication is prohibited for this method.").build());
		}
	}

}
