package edu.swmed.qbrc.auth.cashmac.shared.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CasHmacForeignFieldUpdate {
	String accessLevel();
	Class<?> objectClass();	
}
