package edu.swmed.qbrc.auth.cashmac.shared.annotations;

public @interface CasHmacObjectCreate {
	String accessLevel();
	Class<?> objectClass();	
}
