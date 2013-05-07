package edu.swmed.qbrc.auth.cashmac.shared.annotations;

public @interface CasHmacPreAuth {
	String accessLevel();
	Class<?> objectClass();
	String parameterName();
}
