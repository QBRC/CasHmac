package edu.swmed.qbrc.auth.cashmac.server;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.SecurityContext;
import org.jasig.cas.client.authentication.AttributePrincipal;

public class CasHmacSecurityContextCAS implements SecurityContext {

	private final AttributePrincipal user;
	
	public CasHmacSecurityContextCAS(AttributePrincipal user) {
		this.user = user;
	}
	
	public String getAuthenticationScheme() {
		return SecurityContext.BASIC_AUTH;
	}

	public Principal getUserPrincipal() {
		return this.user;
	}

	public boolean isSecure() {
		return true;
	}

	public boolean isUserInRole(String role) {
		if (user != null) {
			final Map<String, Object> attributes = user.getAttributes();
			if (attributes != null && attributes.containsKey("roles")) {
				@SuppressWarnings("unchecked")
				List<String> roles = (List<String>)attributes.get("roles");
				for (String attrRole : roles) {
					if (attrRole.equals(role))
						return true;
				}
			}
		}
		
		return false;
	}
	
}