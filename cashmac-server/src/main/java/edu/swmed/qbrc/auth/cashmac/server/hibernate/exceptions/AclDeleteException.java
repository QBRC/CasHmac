package edu.swmed.qbrc.auth.cashmac.server.hibernate.exceptions;

import org.hibernate.CallbackException;

public class AclDeleteException extends CallbackException {

	private static final long serialVersionUID = 8595621768913946468L;
	
	public AclDeleteException(Exception exception) {
		super(exception);
	}
	public AclDeleteException(String message) {
		super(message);
	}
	public AclDeleteException(String message, Exception exception) {
		super(message, exception);
	}
}
