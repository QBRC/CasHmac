package edu.swmed.qbrc.auth.cashmac.client;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;
import edu.swmed.qbrc.auth.cashmac.shared.util.HMACUtils;

@Provider
public class ClientAuthInterceptor implements ClientRequestFilter {

	String hostName = null;
	CasHmacRestProvider<?> provider = null;
	
	@Override
	public void filter(ClientRequestContext context) throws IOException {
		try {
			HMACUtils.createSignatureAndSignRequest(context, hostName, provider.getClientId(), provider.getSecret());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setProvider(CasHmacRestProvider<?> provider) {
		this.provider = provider;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

}