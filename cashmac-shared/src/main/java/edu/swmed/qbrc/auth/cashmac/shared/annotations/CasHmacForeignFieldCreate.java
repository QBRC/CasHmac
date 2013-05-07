package edu.swmed.qbrc.auth.cashmac.shared.annotations;

public @interface CasHmacForeignFieldCreate {
	String accessLevel();
	Class<?> objectClass();	
}
