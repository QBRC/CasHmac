package edu.swmed.qbrc.auth.cashmac.shared.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CasHmacPreAuth {
	String accessLevel();
	Class<?> objectClass();
	String parameterName();
}
