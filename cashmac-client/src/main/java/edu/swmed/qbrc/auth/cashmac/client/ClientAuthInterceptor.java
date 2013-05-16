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
	CasHmacRestProvider<?> provider = null;
	
	@SuppressWarnings("rawtypes")
	public ClientResponse execute(ClientExecutionContext context) throws Exception {
		
		HMACUtils.createSignatureAndSignRequest(context.getRequest(), hostName, provider.getClientId(), provider.getSecret());

		return context.proceed();
	}

	public void setProvider(CasHmacRestProvider<?> provider) {
		this.provider = provider;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

}