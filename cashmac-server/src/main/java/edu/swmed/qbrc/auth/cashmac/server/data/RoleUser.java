package edu.swmed.qbrc.auth.cashmac.server.data;

import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;

@TableName(value="roleusers", keycol="roleId")
public class RoleUser implements BaseEntity {
	
	private static final long serialVersionUID = -5978888156116920819L;
	
	private Integer roleId;
    private String username;
    
    public RoleUser() {
        setRoleId(0);
        setUsername("");
    }

    public RoleUser(Integer roleId, String username) {
		super();
		this.setRoleId(roleId);
		this.setUsername(username);
	}

	@Override
	public String toString() {
		return "RoleUser [roleid=" + roleId + ", username=" + username + "]";
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public Object getId() {
		return username + ":" + roleId.toString();
	}
    
}
