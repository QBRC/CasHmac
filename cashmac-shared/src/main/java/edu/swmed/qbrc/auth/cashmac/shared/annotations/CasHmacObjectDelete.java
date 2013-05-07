package edu.swmed.qbrc.auth.cashmac.shared.annotations;

public @interface CasHmacObjectDelete {
	String accessLevel();
	Class<?> objectClass();	
}
