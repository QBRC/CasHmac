package edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors;

import java.io.Serializable;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import edu.swmed.qbrc.auth.cashmac.server.dao.UserDao;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;
import edu.swmed.qbrc.auth.cashmac.server.guice.GuiceModule;
import edu.swmed.qbrc.auth.cashmac.server.hibernate.exceptions.NoAclException;
import edu.swmed.qbrc.auth.cashmac.shared.acl.CrudAclSearch;
import edu.swmed.qbrc.auth.cashmac.shared.acl.CrudAclSearchFactory;
import edu.swmed.qbrc.auth.cashmac.shared.constants.CasHmacAccessLevels;

public class CasHmacInterceptor extends EmptyInterceptor {

	@Inject CrudAclSearchFactory crudAclSearchFactory;
	@Inject UserDao userDao;

	private static final long serialVersionUID = 8287827741641805647L;

	public CasHmacInterceptor() {
		Injector injector = Guice.createInjector(new GuiceModule(CasHmacRequestFilter.getConfig()));
		injector.injectMembers(this);
	}
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		System.out.println("Created: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		return super.onSave(entity, id, state, propertyNames, types);
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		System.out.println("Read: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		CrudAclSearch acl = crudAclSearchFactory.find(entity, CasHmacAccessLevels.READ);
		if (acl.getHasNeccessaryAcl()) {
			return super.onLoad(entity, id, state, propertyNames, types);
		} else {
			throw new NoAclException("No ACL to load entity of type " + entity.getClass().getSimpleName());
		}
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		System.out.println("Updated: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		System.out.println("Deleted: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		super.onDelete(entity, id, state, propertyNames, types);
	}
	
}