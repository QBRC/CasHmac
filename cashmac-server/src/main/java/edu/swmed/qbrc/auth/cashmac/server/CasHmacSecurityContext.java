package edu.swmed.qbrc.auth.cashmac.server;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import edu.swmed.qbrc.auth.cashmac.server.data.Role;
import edu.swmed.qbrc.auth.cashmac.server.data.User;

public class CasHmacSecurityContext implements SecurityContext {

	private final User user;
	
	public CasHmacSecurityContext(User user) {
		this.user = user;
	}
	
	public String getAuthenticationScheme() {
		return SecurityContext.BASIC_AUTH;
	}

	public Principal getUserPrincipal() {
		return user;
	}

	public boolean isSecure() {
		return true;
	}

	public boolean isUserInRole(String role) {
		if (user != null) {
			for (Role userRole : user.getRoles()) {
				if (userRole.getRole().equals(role)) {
					return true;
				}
			}
		}
		return false;
	}
	
}