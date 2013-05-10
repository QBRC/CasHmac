package edu.swmed.qbrc.auth.cashmac.server.acl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import edu.swmed.qbrc.auth.cashmac.server.data.ACL;
import edu.swmed.qbrc.auth.cashmac.server.data.Role;
import edu.swmed.qbrc.auth.cashmac.server.data.User;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;
import edu.swmed.qbrc.auth.cashmac.server.hibernate.exceptions.BadAclRoleException;
import edu.swmed.qbrc.auth.cashmac.shared.annotations.*;
import edu.swmed.qbrc.auth.cashmac.shared.constants.CasHmacAccessLevels;

public class CrudAclSearch {

	private static Boolean DEBUG = false;

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
		 * 
		 * We don't check for a CREATE access level ACL here, since all ACLs are tied to a PK value, and there
		 * isn't a PK yet for a new object.  You'll need to check for CREATE permission with @RolesRequired
		 * annotation on the RESTful method.  Please note, however, that the findPropertyAcls method (called
		 * here) does check for @CasHmacForeignFieldCreate annotations on foreign key fields in the entity.
		 * For example, you could use @CasHmacForeignFieldCreate to configure a Customer object to require
		 * CREATE or UPDATE access to the Store object referenced in its Customer.storeId field. 
		 * 
		 */
		if (casHmacObjectAcl != null && casHmacPKFieldAnn != null) {
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
		if (casHmacWriteAclAnn != null && casHmacPKFieldAnn != null && access.equals(CasHmacAccessLevels.CREATE)) {
			// Get user from session
			User user = (User)CasHmacRequestFilter.getSession().getAttribute("user");
			for (CasHmacWriteAclParameter param : ((CasHmacWriteAcl)casHmacWriteAclAnn.getAnnotation()).value()) {
				
				// Access Level
				String newAclAccess = param.access();
				
				// If no role exist, insert "SELF"
				String[] roles = { "SELF" };
				if (param.roles().length > 0) {
					roles = param.roles();
				}
				
				// For each role
				for (String newAclRole : roles) {
					
					// Create new ACL
					ACL newAcl = new ACL();
					newAcl.setAccess(newAclAccess);

					// If role is "SELF", then set to current user.
					if (newAclRole.equals("SELF")) {
						newAcl.setUsername(user.getId());
					}
					
					// Otherwise, check roles and set roles
					else {
						// Load Role to ensure that it exists
						Role newRole = null;
						try {
							newRole = crudAclSearchFactory.getRoleDao().findByRoleName(newAclRole);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						if (newRole == null) {
							throw new BadAclRoleException("Unable to create ACL for new object; unable to find role " + newAclRole);
						}
						// Set role
						newAcl.setRoleId(newRole.getId());
					}
					
					newAcl.setObjectClass(entity.getClass().getName());
					newAcl.setObjectPK(casHmacPKFieldAnn.getValue());
					try {
						crudAclSearchFactory.getAclDao().put(newAcl);
					} catch (SQLException e) {
						e.printStackTrace();
						throw new BadAclRoleException("Unable to create ACL for new object; SQLException: " + e.getMessage());
					}
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
		
		Boolean bAllFound = false;
		
		/*
		 * First, look at the entity's fields and attempt to gather relevant annotations and field
		 * values from the private fields.
		 */
		for (Field field : entity.getClass().getDeclaredFields()) {
			// If, for some reason, the next two annotations appear more than once, only use the first one.
			if (casHmacPKFieldAnn == null)
				casHmacPKFieldAnn = getPropertyAnnotation(CasHmacPKField.class, field, entity);
			
			if (casHmacWriteAclAnn == null)
				casHmacWriteAclAnn = getPropertyAnnotation(CasHmacWriteAcl.class, field, entity);
			
			// If both annotations have been found, we can escape this loop and continue.
			if (casHmacPKFieldAnn != null && casHmacWriteAclAnn != null) {
				bAllFound = true; // Flag as all found
				break;
			}
		}

		/*
		 * If any relevant annotations were not found above, use java.beans.Introspector to further
		 * reflect upon the entity's getters and setters to find the annotations.
		 */
		if (!bAllFound) {
			
			BeanInfo info = null;
			try {
				info = java.beans.Introspector.getBeanInfo(entity.getClass());
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
			
			if (info != null) {
				for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
					// If, for some reason, the next two annotations appear more than once, only use the first one.
					if (casHmacPKFieldAnn == null)
						casHmacPKFieldAnn = getPropertyAnnotation(CasHmacPKField.class, pd, entity);
					
					if (casHmacWriteAclAnn == null)
						casHmacWriteAclAnn = getPropertyAnnotation(CasHmacWriteAcl.class, pd, entity);
					
					// If both annotations have been found, we can escape this loop and continue.
					if (casHmacPKFieldAnn != null && casHmacWriteAclAnn != null)
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
			for (Field field : entity.getClass().getFields()) {
				// The next four annotations can be applied to any foreign key fields.
				AnnotationAndValue casHmacFFCreateAnn = getPropertyAnnotation(CasHmacForeignFieldCreate.class, field, entity);
				AnnotationAndValue   casHmacFFReadAnn = getPropertyAnnotation(CasHmacForeignFieldRead.class,   field, entity);
				AnnotationAndValue casHmacFFUpdateAnn = getPropertyAnnotation(CasHmacForeignFieldUpdate.class, field, entity);
				AnnotationAndValue casHmacFFDeleteAnn = getPropertyAnnotation(CasHmacForeignFieldDelete.class, field, entity);
				
				// Process foreign key annotations.
				if (returnValue && access.equals(CasHmacAccessLevels.CREATE) && casHmacFFCreateAnn != null)
					if (!getForeignAcl(casHmacFFCreateAnn.getValue(), ((CasHmacForeignFieldCreate)casHmacFFCreateAnn.getAnnotation()).accessLevel()))
						returnValue = false;
				if (returnValue && access.equals(CasHmacAccessLevels.READ) && casHmacFFReadAnn != null)
					if (!getForeignAcl(casHmacFFReadAnn.getValue(), ((CasHmacForeignFieldRead)casHmacFFReadAnn.getAnnotation()).accessLevel()))
						returnValue = false;
				if (returnValue && access.equals(CasHmacAccessLevels.UPDATE) && casHmacFFUpdateAnn != null) 
					if (!getForeignAcl(casHmacFFUpdateAnn.getValue(), ((CasHmacForeignFieldUpdate)casHmacFFUpdateAnn.getAnnotation()).accessLevel()))
						returnValue = false;
				if (returnValue && access.equals(CasHmacAccessLevels.DELETE) && casHmacFFDeleteAnn != null)
					if (!getForeignAcl(casHmacFFDeleteAnn.getValue(), ((CasHmacForeignFieldDelete)casHmacFFDeleteAnn.getAnnotation()).accessLevel()))
						returnValue = false;
			}
			
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				// The next four annotations can be applied to any foreign key fields.
				AnnotationAndValue casHmacFFCreateAnn = getPropertyAnnotation(CasHmacForeignFieldCreate.class, pd, entity);
				AnnotationAndValue   casHmacFFReadAnn =  getPropertyAnnotation(CasHmacForeignFieldRead.class, pd, entity);
				AnnotationAndValue casHmacFFUpdateAnn = getPropertyAnnotation(CasHmacForeignFieldUpdate.class, pd, entity);
				AnnotationAndValue casHmacFFDeleteAnn = getPropertyAnnotation(CasHmacForeignFieldDelete.class, pd, entity);
				
				// Process foreign key annotations.
				if (returnValue && access.equals(CasHmacAccessLevels.CREATE) && casHmacFFCreateAnn != null)
					if (!getForeignAcl(casHmacFFCreateAnn.getValue(), ((CasHmacForeignFieldCreate)casHmacFFCreateAnn.getAnnotation()).accessLevel()))
						returnValue = false;
				if (returnValue && access.equals(CasHmacAccessLevels.READ) && casHmacFFReadAnn != null)
					if (!getForeignAcl(casHmacFFReadAnn.getValue(), ((CasHmacForeignFieldRead)casHmacFFReadAnn.getAnnotation()).accessLevel()))
						returnValue = false;
				if (returnValue && access.equals(CasHmacAccessLevels.UPDATE) && casHmacFFUpdateAnn != null) 
					if (!getForeignAcl(casHmacFFUpdateAnn.getValue(), ((CasHmacForeignFieldUpdate)casHmacFFUpdateAnn.getAnnotation()).accessLevel()))
						returnValue = false;
				if (returnValue && access.equals(CasHmacAccessLevels.DELETE) && casHmacFFDeleteAnn != null)
					if (!getForeignAcl(casHmacFFDeleteAnn.getValue(), ((CasHmacForeignFieldDelete)casHmacFFDeleteAnn.getAnnotation()).accessLevel()))
						returnValue = false;
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
	private Boolean getAcl(String pkvalue, Class<?> objectClass, String accessLevel) {
		// Return false by default (unless valid ACL is found).
		Boolean returnValue = true;
		
		// Get user from session
		User user = (User)CasHmacRequestFilter.getSession().getAttribute("user");

		// Return false right away if user isn't found.
		if (user == null) {
			return false;
		}
		
		
		// Attempt to get ACL from cache
		String key = "entity=" + pkvalue + ";class=" + objectClass.getName() + ";access=" + accessLevel + ";user=" + user.getName();
		List<ACL> acls = crudAclSearchFactory.getCachedACLs(key);
		
		// Debug
		if (acls != null && DEBUG) {
			System.out.println(
					"\n" +
					"--------------------- Retrieved Cached ACL ----------------------\n" +
					"  Key: " + key + "\n" +
					"-----------------------------------------------------------------\n" +
					"\n");
		}
		
		// Get ACL from database.
		if (acls == null) {
			try {
				acls = crudAclSearchFactory.getAclDao().findAcl(user.getName(), accessLevel, objectClass, pkvalue);
				if (acls.size() <= 0)
					returnValue = false;
				else
					crudAclSearchFactory.cacheACL(key, acls);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// Debug
			if (DEBUG) {
				String roleString = "";
				for (Role role : user.getRoles()) {
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
			}
		
		}
		
		// Return true if an ACL was found, otherwise, return false.
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
		private final String value;
		public AnnotationAndValue(Annotation annotation, String value) {
			this.annotation = annotation;
			this.value = value;
		}
		public Annotation getAnnotation() {
			return this.annotation;
		}
		public String getValue() {
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
		Annotation annotation = pd.getReadMethod().getAnnotation(annotationClass);
		String value = null;
		if (annotation != null) {
			try {
				value = pd.getReadMethod().invoke(entity, (Object[])null).toString();
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		if (annotation == null && pd.getWriteMethod() != null)
			annotation = pd.getWriteMethod().getAnnotation(annotationClass);
		if (annotation != null)
			return new AnnotationAndValue(annotation, value);
		else
			return null;
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
	private AnnotationAndValue getPropertyAnnotation(Class<? extends Annotation> annotationClass, Field field, Object entity) {
		Annotation annotation = field.getAnnotation(annotationClass);
		String value = null;
		if (annotation != null) {
			try {
				value = field.get(entity).toString();
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
		if (annotation != null)
			return new AnnotationAndValue(annotation, value);
		else
			return null;
	}

}
