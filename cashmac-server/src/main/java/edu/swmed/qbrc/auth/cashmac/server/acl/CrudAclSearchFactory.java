package edu.swmed.qbrc.auth.cashmac.server.acl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.swmed.qbrc.auth.cashmac.server.acl.utils.CasHmacValidation;
import edu.swmed.qbrc.auth.cashmac.server.dao.ACLDao;
import edu.swmed.qbrc.auth.cashmac.server.dao.RoleDao;
import edu.swmed.qbrc.auth.cashmac.server.dao.UserDao;
import edu.swmed.qbrc.auth.cashmac.server.data.ACL;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;
import edu.swmed.qbrc.auth.cashmac.shared.annotations.CasHmacEntityManagerMap;

public class CrudAclSearchFactory {

	private final ACLDao aclDao;
	private final RoleDao roleDao;
	private final UserDao userDao;
	private final CasHmacValidation casHmacValidation;
	
	private final Map<Class<? extends Annotation>, Provider<EntityManager>> entityManagerProviderMap;
	
	private final Map<String, List<ACL>> aclCache = new HashMap<String, List<ACL>>();
	
	@Inject
	public CrudAclSearchFactory(final ACLDao aclDao, final RoleDao roleDao, final UserDao userDao,
			final CasHmacValidation casHmacValidation,
			@CasHmacEntityManagerMap final Map<Class<? extends Annotation>, Provider<EntityManager>> entityManagerProviderMap) {
		this.aclDao = aclDao;
		this.roleDao = roleDao;
		this.userDao = userDao;
		this.casHmacValidation = casHmacValidation;
		this.entityManagerProviderMap = entityManagerProviderMap;
	}
	
	public CrudAclSearch find(Object entity, Serializable id, String access, Object[] currentState, Object[] previousState, String[] propertyNames) {
		if (this.checkForPreAuth()) 
			return new CrudAclSearch(true);
		else
			return new CrudAclSearch(this, entity, id, access, currentState, previousState, propertyNames);
	}

	public CrudAclSearch find(Object entity, Object id, String access, Class<? extends Annotation> entityManagerAnnotation) {
		return new CrudAclSearch(this, entity, id, access, entityManagerAnnotation);
	}
	
	private Boolean checkForPreAuth() {
		// Mark request as fully validated (or not).
		Boolean retval = false;
		try {
			retval = (Boolean)CasHmacRequestFilter.getRequest().getAttribute("CasHmacValidation.fullyValidated");
			if (retval == null) 
				retval = false;
		} catch (Exception e) {}
		
		return retval; 
	}

	public CrudAclSearch addAcl(Object entity, Serializable id) {
		return new CrudAclSearch(this, entity, id);
	}
	
	public CrudAclSearch deleteAcls(Object entity, Serializable id) {
		return new CrudAclSearch(this, entity, id, true);
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
	
	public CasHmacValidation getCasHmacValidation() {
		return casHmacValidation;
	}
	
	public EntityManager getEntityManager(Class<? extends Annotation> key) {
		return entityManagerProviderMap.get(key).get();
	}
	
}