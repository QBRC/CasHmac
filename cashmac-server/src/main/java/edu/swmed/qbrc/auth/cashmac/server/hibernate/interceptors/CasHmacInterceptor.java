package edu.swmed.qbrc.auth.cashmac.server.hibernate.interceptors;

import java.io.Serializable;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;

public class CasHmacInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 8287827741641805647L;

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		System.out.println("Created: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		return super.onSave(entity, id, state, propertyNames, types);
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		System.out.println("Read: " + entity.getClass().getName() + "\nFrom Session: " + CasHmacRequestFilter.getSession().getId());
		return super.onLoad(entity, id, state, propertyNames, types);
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