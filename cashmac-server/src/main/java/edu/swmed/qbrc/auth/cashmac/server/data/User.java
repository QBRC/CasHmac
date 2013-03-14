package edu.swmed.qbrc.auth.cashmac.server.data;

import java.util.ArrayList;
import java.util.List;
import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;

@TableName(value="users", keycol="id")
public class User implements BaseEntity {
   
	private static final long serialVersionUID = -8426563258191118662L;

    private String id;
    private String secret;
    private final List<Role> roles = new ArrayList<Role>();
    
    public User() {
        id = "";
        secret = "";
    }

    public User(String id, String password, String secret) {
		super();
		this.id = id;
		this.secret = secret;
	}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
    	return this.secret;
    }
    
    public void setSecret(String secret) {
    	this.secret = secret;
    }
    
	@Override
	public String toString() {
		return "User [id=" + id + ", secret=" + secret + "]";
	}
	
	public List<Role> getRoles() {
		return roles;
	}
    
}
