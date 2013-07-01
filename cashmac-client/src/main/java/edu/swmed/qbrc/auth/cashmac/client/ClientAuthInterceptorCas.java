package edu.swmed.qbrc.auth.cashmac.client;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

@Provider
public class ClientAuthInterceptorCas implements ClientRequestFilter {

	String hostName = null;
	CasHmacRestProvider<?> provider = null;
	
	@Override
	public void filter(ClientRequestContext context) throws IOException {
		try {
			/* Get new proxy ticket, using URI as target service */
			Assertion assertion = AssertionHolder.getAssertion();
			String targetService = context.getUri().toASCIIString();
			String proxyTicket = assertion.getPrincipal().getProxyTicketFor(targetService);
			System.out.println("Got proxy ticket............................" + proxyTicket);
			
			/* Create new URI with proxy ticket appended as a query parameter */
			UriBuilder uriBuilder = UriBuilder.fromUri(context.getUri());
			URI newUri = uriBuilder.queryParam("ticket", proxyTicket).build();
			context.setUri(newUri);
			//context.getHeaders().add("proxyTicket", proxyTicket);
			
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