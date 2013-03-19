package edu.swmed.qbrc.auth.cashmac.client;

import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.jboss.resteasy.annotations.interception.ClientInterceptor;

import edu.swmed.qbrc.auth.cashmac.shared.util.HMACUtils;

@Provider
@ClientInterceptor
public class ClientAuthInterceptor implements ClientExecutionInterceptor {

	String hostName = null;
	String clientId = null;
	String secret = null;
	Boolean needsFormParametersCleared = false;
	
	@SuppressWarnings("rawtypes")
	public ClientResponse execute(ClientExecutionContext context) throws Exception {
		
		// Hack for removing form parameters for GWT applications.  GWT apparently adds form params to requests,
		// and if RESTEasy sees any form parameters, it will assume that we need an HttpPost at some point, which
		// will result in an error like:
		//   org.apache.http.client.methods.HttpGet cannot be cast to org.apache.http.client.methods.HttpPost
		if (needsFormParametersCleared) {
			context.getRequest().getFormParameters().clear();
		}

		HMACUtils.createSignatureAndSignRequest(context.getRequest(), hostName, clientId, secret);
		
		return context.proceed();
	}
	
	public void setNeedsFormParametersCleared(Boolean needsFormParametersCleared) {
		this.needsFormParametersCleared = needsFormParametersCleared;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
	}

}