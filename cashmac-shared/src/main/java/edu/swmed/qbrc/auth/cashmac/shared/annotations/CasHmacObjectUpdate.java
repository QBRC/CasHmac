package edu.swmed.qbrc.auth.cashmac.shared.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CasHmacObjectUpdate {
	String accessLevel();
	Class<?> objectClass();	
}