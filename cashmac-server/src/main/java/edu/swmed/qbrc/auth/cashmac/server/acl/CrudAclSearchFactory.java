package edu.swmed.qbrc.auth.cashmac.server.acl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.dao.ACLDao;
import edu.swmed.qbrc.auth.cashmac.server.dao.RoleDao;
import edu.swmed.qbrc.auth.cashmac.server.dao.UserDao;
import edu.swmed.qbrc.auth.cashmac.server.data.ACL;

public class CrudAclSearchFactory {

	private final ACLDao aclDao;
	private final RoleDao roleDao;
	private final UserDao userDao;
	
	private final Map<String, List<ACL>> aclCache = new HashMap<String, List<ACL>>();
	
	@Inject
	public CrudAclSearchFactory(final ACLDao aclDao, final RoleDao roleDao, final UserDao userDao) {
		this.aclDao = aclDao;
		this.roleDao = roleDao;
		this.userDao = userDao;
	}
	
	public CrudAclSearch find(Object entity, String access) {
		return new CrudAclSearch(this, entity, access);
	}
	
	public void cacheACL(String key, List<ACL> acls) {
		aclCache.put(key, acls);
	}
	
	public List<ACL> getCachedACLs(String key) {
		return aclCache.get(key);
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