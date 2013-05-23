package edu.swmed.qbrc.auth.cashmac.server.acl.utils;

import java.io.Serializable;

public class NoInterceptionWrapper implements Serializable {

	private static final long serialVersionUID = -473877830815058372L;

	private final Serializable value;
	
	public NoInterceptionWrapper(Serializable value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}

}
