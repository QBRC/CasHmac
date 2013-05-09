package edu.swmed.qbrc.auth.cashmac.server.acl;

import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.dao.ACLDao;
import edu.swmed.qbrc.auth.cashmac.server.dao.RoleDao;
import edu.swmed.qbrc.auth.cashmac.server.dao.UserDao;

public class CrudAclSearchFactory {

	private final ACLDao aclDao;
	private final RoleDao roleDao;
	private final UserDao userDao;
	
	@Inject
	public CrudAclSearchFactory(final ACLDao aclDao, final RoleDao roleDao, final UserDao userDao) {
		this.aclDao = aclDao;
		this.roleDao = roleDao;
		this.userDao = userDao;
	}
	
	public CrudAclSearch find(Object entity, String access) {
		return new CrudAclSearch(this, entity, access);
	}

	public ACLDao getAclDao() {
		return aclDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
}