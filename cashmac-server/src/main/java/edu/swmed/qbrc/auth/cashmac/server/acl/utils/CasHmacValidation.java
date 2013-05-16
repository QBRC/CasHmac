package edu.swmed.qbrc.auth.cashmac.server.acl.utils;

import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.acl.CrudAclSearchFactory;
import edu.swmed.qbrc.auth.cashmac.server.dao.ACLDao;
import edu.swmed.qbrc.auth.cashmac.server.dao.RoleDao;
import edu.swmed.qbrc.auth.cashmac.server.data.ACL;
import edu.swmed.qbrc.auth.cashmac.server.data.Role;
import edu.swmed.qbrc.auth.cashmac.server.data.User;
import edu.swmed.qbrc.auth.cashmac.server.filters.CasHmacRequestFilter;
import edu.swmed.qbrc.auth.cashmac.shared.exceptions.AclDeleteException;
import edu.swmed.qbrc.auth.cashmac.shared.exceptions.BadAclRoleException;
import edu.swmed.qbrc.auth.cashmac.shared.exceptions.NoAclException;

public class CasHmacValidation {
	
	private static final Logger log = Logger.getLogger(CasHmacValidation.class);
	
	private final ACLDao aclDao;
	private final RoleDao roleDao;

	@Inject
	public CasHmacValidation(final ACLDao aclDao, final RoleDao roleDao) {
		this.aclDao = aclDao;
		this.roleDao = roleDao;
	}
	
	/**
	 * Gets an ACL by calling verifyAcl, but additionally flags the request as
	 * validated, so that further validation is not performed.
	 * 
	 * @param access
	 * @param objectClass
	 * @param pkValue
	 * @param factory (optional)
	 * @return
	 */
	public Boolean preAuthAcl(String access, Class<?> objectClass, Object pkValue, CrudAclSearchFactory factory) {
		// Get response
		Boolean retval = verifyAcl(access, objectClass, pkValue, factory);
		
		// Mark request as fully validated (or not).
		CasHmacRequestFilter.getRequest().setAttribute("CasHmacValidation.fullyValidated", retval);
		
		return retval;
	}
	
	/**
	 * Gets the actual ACL (if any match) from the database.  This is the only
	 * method that reads ACLS from the database.  We also cache the ACL to
	 * ensure that any subsequent calls for the same ACL are more efficient.
	 * 
	 * @param access
	 * @param objectClass
	 * @param pkValue
	 * @param factory (optional)
	 * @return
	 */
	public Boolean verifyAcl(String access, Class<?> objectClass, Object pkValue, CrudAclSearchFactory factory) {
		// Return false by default (unless valid ACL is found).
		Boolean returnValue = true;
		
		// Get user from session
		User user = (User)CasHmacRequestFilter.getRequest().getAttribute("user");

		// Return false right away if user isn't found.
		if (user == null) {
			return false;
		}
		
		
		// Attempt to get ACL from cache
		List<ACL> acls = null;
		String key = "entity=" + pkValue + ";class=" + objectClass.getName() + ";access=" + access + ";user=" + user.getName();
		if (factory != null) {
			acls = factory.getCachedACLs(key);
		}
		
		// Debug
		if (acls != null) {
			log.trace(
					"\n" +
					"--------------------- Retrieved Cached ACL ----------------------\n" +
					"  Key: " + key + "\n" +
					"-----------------------------------------------------------------\n" +
					"\n"
			);
		}
		
		// Get ACL from database.
		if (acls == null) {
			try {
				acls = aclDao.findAcl(user.getName(), access, objectClass, pkValue);
				if (acls.size() <= 0)
					returnValue = false;
				else if (factory != null)
					factory.cacheACL(key, acls);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// Debug
			String roleString = "";
			for (Role role : user.getRoles()) {
				roleString += roleString.equals("") ? "[ " : ",";
				roleString += role.getRole();
			}
			log.trace(
					"\n" +
					"-------------------------- Getting ACL --------------------------\n" +
					"  Class:  " + objectClass.getName() + "\n" +
					"  Access: " + access + "\n" +
					"  Entity: " + pkValue + "\n" +
					"  User:   " + user.getName() + "\n" +
					"  Roles:  " + roleString + " ]\n" +
					"-----------------------------------------------------------------\n" +
					"\n");
		
		}
		
		// Return true if an ACL was found, otherwise, return false.
		return returnValue;
	}
	
	/**
	 * Adds an ACL to the database.
	 * 
	 * @param access
	 * @param objectClass
	 * @param pkValue
	 * @param roles
	 */
	public void addAcl(String access, Class<?> objectClass, Object pkValue, String[] roles) {
		// Get user from session
		User user = (User)CasHmacRequestFilter.getRequest().getAttribute("user");
			
		// If no role exist, insert "SELF"
		String[] rolesToAdd = { "SELF" };
		if (roles.length > 0) {
			rolesToAdd = roles;
		}
			
		// For each role
		for (String newAclRole : rolesToAdd) {
			
			// Create new ACL
			ACL newAcl = new ACL();
			newAcl.setAccess(access);

			// If role is "SELF", then set to current user.
			if (newAclRole.equals("SELF")) {
				newAcl.setUsername(user.getId());
			}
			
			// Otherwise, check roles and set roles
			else {
				// Load Role to ensure that it exists
				Role newRole = null;
				try {
					newRole =roleDao.findByRoleName(newAclRole);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				if (newRole == null) {
					throw new BadAclRoleException("Unable to create ACL for new object; unable to find role " + newAclRole);
				}
				// Set role
				newAcl.setRoleId(newRole.getId());
			}
			
			newAcl.setObjectClass(objectClass.getName());
			newAcl.setObjectPK(pkValue);
			try {
				aclDao.put(newAcl);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new BadAclRoleException("Unable to create ACL for new object; SQLException: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Deletes ACLS.  If no roles are specified, we assume that we're deleting the specifiec ACL/s
	 * for the current user.
	 * 
	 * @param access
	 * @param objectClass
	 * @param pkValue
	 * @param roles
	 */
	public void deleteAcl(String access, Class<?> objectClass, Object pkValue, String[] roles) {
		// Get user from session
		User user = (User)CasHmacRequestFilter.getRequest().getAttribute("user");
			
		// If no role exist, insert "SELF"
		String[] rolesToDelete = { "SELF" };
		if (roles.length > 0) {
			rolesToDelete = roles;
		}

		// Get ACL from database.
		List<ACL> acls = null;
		try {
			acls = aclDao.findAclNonUserSpecific(access, objectClass, pkValue);
		} catch (SQLException e) {
			throw new AclDeleteException("Unable to delete ACL: " + e.getMessage());
		}

		// Ensure that we actually found an ACL
		if (acls == null) {
			throw new NoAclException("Unable to find the ACL to delete (access=" + access + "; class=" + objectClass.getName() + "; pkValue=" + pkValue + ")");
		}
		
			
		// Loop through all roles specified
		for (String role : rolesToDelete) {
			
			// If dealing with user's own ACL
			if (role.trim().equals("SELF")) {
				// Loop through all ACLs found
				for (ACL acl : acls) {
					// Find ACLs for the current user
					if (acl.getUsername() != null && acl.getUsername().equals(user.getId())) {
						// Delete Matched ACL
						try {
							aclDao.delete(acl.getId());
						} catch (SQLException e) {
							throw new AclDeleteException("Unable to delete ACL: " + e.getMessage());
						}
					}
				}
			}
			
			// Otherwise, check for matching roles for the ACL
			else {
				// Load role
				Role daoRole;
				try {
					daoRole = roleDao.findByRoleName(role.trim());
				} catch (SQLException e) {
					throw new AclDeleteException("Unable to delete ACL: " + e.getMessage());
				}
				
				// Loop through all ACLs found
				for (ACL acl : acls) {
					// Find ACLs for the current user
					if (acl.getRoleId() == daoRole.getId()) {
						// Delete Matched ACL
						try {
							aclDao.delete(acl.getId());
						} catch (SQLException e) {
							throw new AclDeleteException("Unable to delete ACL: " + e.getMessage());
						}
					}
				}
			}
			
		}
	}

	/**
	 * Deletes all ACLS for an object.
	 * 
	 * @param objectClass
	 * @param pkValue
	 */
	public void deleteAllAclsForObject(Class<?> objectClass, Object pkValue) {
			
		// Get ACL from database.
		List<ACL> acls = null;
		try {
			acls = aclDao.findObjectAcls(objectClass, pkValue);
		} catch (SQLException e) {
			throw new AclDeleteException("Unable to delete ACL: " + e.getMessage());
		}

		// Ensure that we actually found an ACL
		if (acls == null) {
			throw new NoAclException("Unable to find the ACL to delete (class=" + objectClass.getName() + "; pkValue=" + pkValue + ")");
		}
		
		for (ACL acl : acls) {
			// Delete Matched ACL
			try {
				aclDao.delete(acl.getId());
			} catch (SQLException e) {
				throw new AclDeleteException("Unable to delete ACL: " + e.getMessage());
			}
		}
	}

}
