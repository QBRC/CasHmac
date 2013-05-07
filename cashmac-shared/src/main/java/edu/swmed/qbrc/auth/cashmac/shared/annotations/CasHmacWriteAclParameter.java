package edu.swmed.qbrc.auth.cashmac.shared.annotations;

public @interface CasHmacWriteAclParameter {
	public String access();
	public String[] roles();
}
