package edu.swmed.qbrc.auth.cashmac.server.hibernate.exceptions;

import org.hibernate.CallbackException;

public class BadAclRoleException extends CallbackException {

	private static final long serialVersionUID = -8714042400096239234L;

	public BadAclRoleException(Exception exception) {
		super(exception);
	}
	public BadAclRoleException(String message) {
		super(message);
	}
	public BadAclRoleException(String message, Exception exception) {
		super(message, exception);
	}
}
