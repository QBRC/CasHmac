package edu.swmed.qbrc.auth.cashmac.server.data;

import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;
import edu.swmed.qbrc.auth.cashmac.shared.data.BaseEntity;

@TableName(value="acl", keycol="id")
public class ACL implements BaseEntity {
   
	private static final long serialVersionUID = 5412954895376547005L;

    private Integer id;    
    private String username;
    private Integer roleId;
    private String access;
    private String objectClass;
    private String objectPK;
    
    public ACL() {    	
		this.setId(null);
		this.setUsername(null);
		this.setRoleId(null);
    }
    
	public ACL(Integer id, String username, Integer roleId, String access, String objectClass, String objectPK) {
		super();
		this.setId(id);
		this.setUsername(username);
		this.setRoleId(roleId);
		this.setAccess(access);
		this.setObjectClass(objectClass);
		this.setObjectPK(objectPK);
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public String getObjectPK() {
		return objectPK;
	}

	public void setObjectPK(String objectPK) {
		this.objectPK = objectPK;
	}
    
	@Override
	public String toString() {
		return "ACL";
	}
    
}
