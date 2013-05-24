package edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors;

import java.io.Serializable;
import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import com.google.inject.Guice;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.acl.CrudAclSearch;
import edu.swmed.qbrc.auth.cashmac.server.acl.CrudAclSearchFactory;
import edu.swmed.qbrc.auth.cashmac.server.dao.UserDao;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;
import edu.swmed.qbrc.auth.cashmac.server.guice.GuiceModule;
import edu.swmed.qbrc.auth.cashmac.server.guice.MainGuiceModule;
import edu.swmed.qbrc.auth.cashmac.shared.constants.CasHmacAccessLevels;
import edu.swmed.qbrc.auth.cashmac.shared.exceptions.NoAclException;

/**
 * Here we intercept Hibernate events. These are all "pre" interceptors, meaning that
 * they run before the pending action takes place.  Hence, we use these interceptors
 * to check for the necessary ACLs before performing the action.
 * 
 * For actions that require "post" interceptors, please see the CasHmacIntegrator class,
 * which sets up post delete and post insert interceptors (to delete ACLs and insert new
 * ACLs, respectively).
 * 
 * @author JYODE1
 *
 */
public class CasHmacInterceptor extends EmptyInterceptor {

	@Inject CrudAclSearchFactory crudAclSearchFactory;
	@Inject UserDao userDao;

	private static final long serialVersionUID = 8287827741641805647L;
	
	private static final Logger log = Logger.getLogger(CasHmacInterceptor.class);
	
	public CasHmacInterceptor() {
		GuiceModule guiceModule = Guice.createInjector(MainGuiceModule.getModule()).getInstance(GuiceModule.class);
		Guice.createInjector(guiceModule).injectMembers(this);
	}
	
	/**
	 * Before an entity is INSERTED.
	 */
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		log.trace("Created: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		CrudAclSearch acl = crudAclSearchFactory.find(entity, null, CasHmacAccessLevels.CREATE, state, null, propertyNames);
		if (acl.getHasNeccessaryAcl()) {
			return super.onSave(entity, id, state, propertyNames, types);
		} else {
			throw new NoAclException("No ACL to insert entity of type " + entity.getClass().getSimpleName());
		}
	}

	
	/**
	 * Before an entity is LOADED.
	 */
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		log.trace("Read: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId() + "\nNumber of Properties: " + propertyNames.length);
		CrudAclSearch acl = crudAclSearchFactory.find(entity, id, CasHmacAccessLevels.READ, state, null, propertyNames);
		if (acl.getHasNeccessaryAcl()) {
			return super.onLoad(entity, id, state, propertyNames, types);
		} else {
			throw new NoAclException("No ACL to load entity of type " + entity.getClass().getSimpleName());
		}
	}

	/**
	 * Before an existing entity is UPDATED.
	 */
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		log.trace("Updated: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		CrudAclSearch acl = null;
		try {
			acl = crudAclSearchFactory.find(entity, id, CasHmacAccessLevels.UPDATE, currentState, previousState, propertyNames);
		} catch (NoAclException e) {
			log.trace("Error: NoAclException while looking up ACLs for " + entity.getClass().getSimpleName() + " " + id);
			throw e;
		}
		if (acl != null && acl.getHasNeccessaryAcl()) {
			return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
		} else {
			log.trace("Error: unable to update entity " + entity.getClass().getSimpleName() + " " + id);
			throw new NoAclException("No ACL to update entity of type " + entity.getClass().getSimpleName());
		}
	}

	/**
	 * Before an entity is DELETED.
	 */
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		log.trace("Deleted: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		CrudAclSearch acl = crudAclSearchFactory.find(entity, id, CasHmacAccessLevels.DELETE, state, null, propertyNames);
		if (acl.getHasNeccessaryAcl()) {
			super.onDelete(entity, id, state, propertyNames, types);
		} else {
			throw new NoAclException("No ACL to delete entity of type " + entity.getClass().getSimpleName());
		}
	}
	
}