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
	
	@SuppressWarnings("rawtypes")
	public ClientResponse execute(ClientExecutionContext context) throws Exception {
		
		HMACUtils.createSignatureAndSignRequest(context.getRequest(), hostName, clientId, secret);
		return context.proceed();
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