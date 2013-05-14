package edu.swmed.qbrc.auth.cashmac.shared.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CasHmacForeignFieldRead {
	String accessLevel();
	Class<?> objectClass();	
	Class<? extends Annotation> foreignEntityManager();
}
