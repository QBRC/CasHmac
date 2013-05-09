package edu.swmed.qbrc.auth.cashmac.server.acl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import edu.swmed.qbrc.auth.cashmac.server.data.ACL;
import edu.swmed.qbrc.auth.cashmac.server.data.Role;
import edu.swmed.qbrc.auth.cashmac.server.data.User;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;
import edu.swmed.qbrc.auth.cashmac.shared.annotations.*;
import edu.swmed.qbrc.auth.cashmac.shared.constants.CasHmacAccessLevels;

public class CrudAclSearch {

	private final CrudAclSearchFactory crudAclSearchFactory;
	private final Boolean hasNeccessaryAcl;
	
	// The two following annotations are expected only once per class.
	private AnnotationAndValue casHmacPKFieldAnn = null;
	private AnnotationAndValue casHmacWriteAclAnn = null;

	public CrudAclSearch(CrudAclSearchFactory crudAclSearchFactory, Object entity, String access) {
		this.crudAclSearchFactory = crudAclSearchFactory; 
		this.hasNeccessaryAcl = searchForAcl(entity, access);
	}

	/**
	 * Search for appropriate ACLs and set object properties accordingly.
	 * @param entity
	 * @param access
	 */
	private Boolean searchForAcl(Object entity, String access) {
		return
				findClassAcls(entity, access) &&
				findForeignKeyAcls(entity, access);
	}

	public Boolean getHasNeccessaryAcl() {
		return hasNeccessaryAcl;
	}
	
	/**
	 * Process ACLs for annotations applied directly to a class.
	 * @param entity
	 * @param access
	 * @return
	 */
	private Boolean findClassAcls(Object entity, String access) {
		CasHmacObjectAcl casHmacObjectAcl = entity.getClass().getAnnotation(CasHmacObjectAcl.class);
		CasHmacObjectCreate casHmacObjectCreate = entity.getClass().getAnnotation(CasHmacObjectCreate.class);
		CasHmacObjectRead casHmacObjectRead = entity.getClass().getAnnotation(CasHmacObjectRead.class);
		CasHmacObjectUpdate casHmacObjectUpdate = entity.getClass().getAnnotation(CasHmacObjectUpdate.class);
		CasHmacObjectDelete casHmacObjectDelete = entity.getClass().getAnnotation(CasHmacObjectDelete.class);
		
		// Return true by default (unless we found unfulfilled ACLs)
		Boolean returnValue = true;

		// Initialize fields
		findPropertyAcls(entity, access);

		/*
		 *  If an object doesn't have an Object ACL, we know immediately that no ACLs will be read or written
		 * for the object directly.  There could still be, however, foreign key ACLs for this object.
		 * Process CRUD ACLs for this object only if the class has an Object ACL.
		 */
		if (casHmacObjectAcl != null) {
			if (returnValue && access.equals(CasHmacAccessLevels.CREATE) && casHmacObjectCreate != null) {
				if (! getAcl(casHmacPKFieldAnn.getValue(), casHmacObjectCreate.objectClass(), casHmacObjectCreate.accessLevel()))
					returnValue = false;
			}
			if (returnValue && access.equals(CasHmacAccessLevels.READ) && casHmacObjectRead != null) {
				if (! getAcl(casHmacPKFieldAnn.getValue(), casHmacObjectRead.objectClass(), casHmacObjectRead.accessLevel()))
					returnValue = false;
			}
			if (returnValue && access.equals(CasHmacAccessLevels.UPDATE) && casHmacObjectUpdate != null) {
				if (! getAcl(casHmacPKFieldAnn.getValue(), casHmacObjectUpdate.objectClass(), casHmacObjectUpdate.accessLevel()))
					returnValue = false;
			}
			if (returnValue && access.equals(CasHmacAccessLevels.DELETE) && casHmacObjectDelete != null) {
				if (! getAcl(casHmacPKFieldAnn.getValue(), casHmacObjectDelete.objectClass(), casHmacObjectDelete.accessLevel()))
					returnValue = false;
			}
		}

		// Process Write ACLs (ACLs to write when object is saved) if saving a new item
		if (casHmacWriteAclAnn != null && casHmacWriteAclAnn.getAnnotation() != null && access == CasHmacAccessLevels.CREATE) {
			for (CasHmacWriteAclParameter param : ((CasHmacWriteAcl)casHmacWriteAclAnn.getAnnotation()).value()) {
				param.access();
				for (String role : param.roles()) {
					//TODO write the new ACL here.
					role = role + ""; // Remove me.
				}
			}
		}
		
		return returnValue;
	}
	
	/**
	 * Find annotations applied to object properties.  Be sure to use this method before the
	 * findClassAcls method, as this method sets the casHmacPKField and casHmacWriteAcl fields
	 * of this class, which are needed in the findClassAcls method.
	 * @param entity
	 * @param access
	 * @return
	 */
	private void findPropertyAcls(Object entity, String access) {
		BeanInfo info = null;
		try {
			info = java.beans.Introspector.getBeanInfo(entity.getClass());
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		
		if (info != null) {
			
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				// If, for some reason, the next two annotations appear more than once, only use the first one.
				if (casHmacPKFieldAnn == null) {
					AnnotationAndValue temp1 = getPropertyAnnotation(CasHmacPKField.class, pd, entity);
					if (temp1.getAnnotation() != null && temp1.getValue() != null)
						casHmacPKFieldAnn = temp1;
				}
				if (casHmacWriteAclAnn == null) {
					AnnotationAndValue temp2 = getPropertyAnnotation(CasHmacWriteAcl.class, pd, entity);
					if (temp2.getAnnotation() != null && temp2.getValue() != null)
						casHmacWriteAclAnn = temp2;
				}
				
				// If both annotations have been found, we can escape this loop and continue.
				if (casHmacPKFieldAnn != null && casHmacWriteAclAnn != null &&
						casHmacPKFieldAnn.getAnnotation() != null && casHmacWriteAclAnn.getAnnotation() != null)
				{
					break;
				}
			}
		}		
	}
	
	/**
	 * Find annotations applied to object properties that are foreign keys.  
	 * @param entity
	 * @param access
	 * @return
	 */
	private Boolean findForeignKeyAcls(Object entity, String access) {
		BeanInfo info = null;
		try {
			info = java.beans.Introspector.getBeanInfo(entity.getClass());
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		
		// Return true by default (unless we found unfulfilled ACLs)
		Boolean returnValue = true;
		
		if (info != null) {
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				// The next four annotations can be applied to any foreign key fields.
				AnnotationAndValue casHmacFFCreateAnn = getPropertyAnnotation(CasHmacForeignFieldCreate.class, pd, entity);
				AnnotationAndValue   casHmacFFReadAnn =  getPropertyAnnotation(CasHmacForeignFieldRead.class, pd, entity);
				AnnotationAndValue casHmacFFUpdateAnn = getPropertyAnnotation(CasHmacForeignFieldUpdate.class, pd, entity);
				AnnotationAndValue casHmacFFDeleteAnn = getPropertyAnnotation(CasHmacForeignFieldDelete.class, pd, entity);
				
				// Process foreign key annotations.
				if (returnValue && access.equals(CasHmacAccessLevels.CREATE) && casHmacFFCreateAnn.getAnnotation() != null) {
					CasHmacForeignFieldCreate ann = (CasHmacForeignFieldCreate)casHmacFFCreateAnn.getAnnotation();
					if (!getForeignAcl(casHmacFFUpdateAnn.getValue(), ann.accessLevel()))
						returnValue = false;
				}
				if (returnValue && access.equals(CasHmacAccessLevels.READ) && casHmacFFReadAnn.getAnnotation() != null) {
					CasHmacForeignFieldRead ann = (CasHmacForeignFieldRead)casHmacFFReadAnn.getAnnotation();
					if (!getForeignAcl(casHmacFFUpdateAnn.getValue(), ann.accessLevel()))
						returnValue = false;
				}
				if (returnValue && access.equals(CasHmacAccessLevels.UPDATE) && casHmacFFUpdateAnn.getAnnotation() != null) { 
					CasHmacForeignFieldUpdate ann = (CasHmacForeignFieldUpdate)casHmacFFUpdateAnn.getAnnotation();
					if (!getForeignAcl(casHmacFFUpdateAnn.getValue(), ann.accessLevel()))
						returnValue = false;
				}
				if (returnValue && access.equals(CasHmacAccessLevels.DELETE) && casHmacFFDeleteAnn.getAnnotation() != null) {
					CasHmacForeignFieldDelete ann = (CasHmacForeignFieldDelete)casHmacFFDeleteAnn.getAnnotation();
					if (!getForeignAcl(casHmacFFUpdateAnn.getValue(), ann.accessLevel()))
						returnValue = false;
				}
			}
		}		
		
		return returnValue;
	}


	/**
	 * Looks up an ACL for a foreign field.  We do this by simply creating a new ACL
	 * from the CrudAclSearchFactory.  This ensures that any cascading dependencies
	 * are considered in order, and it ensures that the ACL is cached.
	 * @param foreignEntity
	 * @param accessLevel
	 * @return
	 */
	private Boolean getForeignAcl(Object foreignEntity, String accessLevel) {
		Boolean returnValue = true;
		
		if (foreignEntity != null) {
			CrudAclSearch subAcl = crudAclSearchFactory.find(foreignEntity, accessLevel);
			if (! subAcl.getHasNeccessaryAcl())
				returnValue = false;
		}
		
		return returnValue;
	}
	
	/**
	 * Gets the actual ACL (if any match) from the database.  This is the only
	 * method that reads ACLS from the database.  We also cache the ACL to
	 * ensure that any subsequent calls for the same ACL are more efficient.
	 * @param entity
	 * @param objectClass
	 * @param accessLevel
	 * @return
	 */
	private Boolean getAcl(Object pkvalue, Class<?> objectClass, String accessLevel) {
		// Return false by default (unless valid ACL is found).
		Boolean returnValue = true;
		
		// Get user from session
		User user = (User)CasHmacRequestFilter.getSession().getAttribute("user");
		List<Role> roles = user.getRoles();
		
		// Get ACL from database.
		List<ACL> acls = null;
		try {
			
			acls = crudAclSearchFactory.getAclDao().findAcl(user.getName(), roles, accessLevel, objectClass, pkvalue);
			if (acls.size() <= 0)
				returnValue = false;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Debug
		String roleString = "";
		for (Role role : roles) {
			roleString += roleString.equals("") ? "[ " : ",";
			roleString += role.getRole();
		}
		System.out.println(
				"\n" +
				"-------------------------- Getting ACL --------------------------\n" +
				"  Class:  " + objectClass.getName() + "\n" +
				"  Access: " + accessLevel + "\n" +
				"  Entity: " + pkvalue + "\n" +
				"  User:   " + user.getName() + "\n" +
				"  Roles:  " + roleString + " ]\n" +
				"-----------------------------------------------------------------\n" +
				"\n");
		
		return returnValue;
	}
	
	/**
	 * Private class used to return both an annotation and the property value
	 * of the field it annotates from the getPropertyAnnotation method.
	 * @author JYODE1
	 *
	 */
	private class AnnotationAndValue {
		private final Annotation annotation;
		private final Object value;
		public AnnotationAndValue(Annotation annotation, Object value) {
			this.annotation = annotation;
			this.value = value;
		}
		public Annotation getAnnotation() {
			return this.annotation;
		}
		public Object getValue() {
			return this.value;
		}
	}
	
	/**
	 * Returns an annotation for a field.  It looks at the following items (in order) while attempting
	 * to find the annotation:
	 * 	1. Field
	 *  2. Getter
	 *  3. Setter
	 * @param annotationClass
	 * @param pd
	 * @return
	 */
	private AnnotationAndValue getPropertyAnnotation(Class<? extends Annotation> annotationClass, PropertyDescriptor pd, Object entity) {
		Annotation annotation = pd.getPropertyType().getAnnotation(annotationClass);
		Object value = null;
		if (annotation == null && pd.getReadMethod() != null) {
			annotation = pd.getReadMethod().getAnnotation(annotationClass);
			try {
				value = (Object)pd.getReadMethod().invoke(entity, (Object[])null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if (annotation == null && pd.getWriteMethod() != null)
			annotation = pd.getWriteMethod().getAnnotation(annotationClass);
		return new AnnotationAndValue(annotation, value);
	}
	
}
