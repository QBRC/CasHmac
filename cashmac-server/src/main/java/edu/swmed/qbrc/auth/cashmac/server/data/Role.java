package edu.swmed.qbrc.auth.cashmac.server.data;

import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;

@TableName(value="roles", keycol="id")
public class Role implements BaseEntity {
   
	private static final long serialVersionUID = 4809021141402641492L;
	
	private Integer id;
    private String role;
    
    public Role() {
        id = 0;
        role = "";
    }

    public Role(Integer id, String role) {
		super();
		this.id = id;
		this.role = role;
	}


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

   
    public String getRole() {
    	return this.role;
    }
    
    public void setRole(String role) {
    	this.role = role;
    }
    
	@Override
	public String toString() {
		return "Role [id=" + id + ", role=" + role + "]";
	}
    
}
