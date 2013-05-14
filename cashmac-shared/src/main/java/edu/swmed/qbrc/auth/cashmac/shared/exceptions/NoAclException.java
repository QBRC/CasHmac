package edu.swmed.qbrc.auth.cashmac.shared.exceptions;

import org.hibernate.CallbackException;

public class NoAclException extends CallbackException {

	private static final long serialVersionUID = -3615154049236689273L;

	public NoAclException(Exception exception) {
		super(exception);
	}
	public NoAclException(String message) {
		super(message);
	}
	public NoAclException(String message, Exception exception) {
		super(message, exception);
	}
}
