package edu.swmed.qbrc.auth.cashmac.server.data;

import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;
import edu.swmed.qbrc.auth.cashmac.shared.data.BaseEntity;

@TableName(value="roles", keycol="id")
public class Role implements BaseEntity {
   
	private static final long serialVersionUID = 4809021141402641492L;
	
	private Integer id;
	private String username;
    private String role;
    
    public Role() {
        id = 0;
        username = "";
        role = "";
    }

    public Role(Integer id, String username, String role) {
		super();
		this.id = id;
		this.username = username;
		this.role = role;
	}


    public Integer getId() {
        return id;
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

    
    public String getRole() {
    	return this.role;
    }
    
    public void setRole(String role) {
    	this.role = role;
    }
    
	@Override
	public String toString() {
		return "Role [id=" + id + ", username=" + username + ", role=" + role + "]";
	}
    
}
