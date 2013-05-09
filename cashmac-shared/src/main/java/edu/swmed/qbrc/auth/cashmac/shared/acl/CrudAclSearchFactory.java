package edu.swmed.qbrc.auth.cashmac.shared.acl;

public class CrudAclSearchFactory {

	public CrudAclSearchFactory() {
	}
	
	public CrudAclSearch find(Object entity, String access) {
		return new CrudAclSearch(this, entity, access);
	}
	
}