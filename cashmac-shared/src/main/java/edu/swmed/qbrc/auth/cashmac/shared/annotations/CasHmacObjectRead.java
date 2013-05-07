package edu.swmed.qbrc.auth.cashmac.shared.annotations;

public @interface CasHmacObjectRead {
	String accessLevel();
	Class<?> objectClass();	
}
