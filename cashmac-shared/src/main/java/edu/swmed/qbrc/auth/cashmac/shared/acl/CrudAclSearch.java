package edu.swmed.qbrc.auth.cashmac.shared.acl;

public class CrudAclSearch<T> {

	private final Boolean hasNeccessaryAcl;
	
	/**
	 * Constructor: When a new object is created, immediately search for appropriate ACLs
	 * and set object properties accordingly.
	 * @param object
	 * @param access
	 */
	public CrudAclSearch(T object, String access) {
		hasNeccessaryAcl = false;
	}

	public Boolean getHasNeccessaryAcl() {
		return hasNeccessaryAcl;
	}
	
}
