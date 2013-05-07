package edu.swmed.qbrc.auth.cashmac.shared.annotations;

public @interface CasHmacForeignFieldRead {
	String accessLevel();
	Class<?> objectClass();	
}
