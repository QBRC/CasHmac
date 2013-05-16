package edu.swmed.qbrc.auth.cashmac.server.dao;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;
import edu.swmed.qbrc.auth.cashmac.server.data.Role;
import edu.swmed.qbrc.auth.cashmac.server.data.User;

public class UserDao extends BaseDao<User> {
	
	private final RoleDao roleDao;
	
	@Inject
	public UserDao(RoleDao roleDao, final Map<String, String> servletConfig, final BasicDataSource dataSource) {
		super(User.class, servletConfig, dataSource);
		this.roleDao = roleDao;
    }

	@Override
	public User load(Object id) throws SQLException {
		User toReturn = super.load(id);
		toReturn.getRoles().addAll(roleDao.findByUsername(toReturn.getId()));
		return toReturn;
	}
	
    public User findByUsername(String username) throws SQLException {
    	return load(username);
    }

	@Override
	public User setData(ResultSet results) throws SQLException {
        // Get Context Parameters for Role table information
    	String keycol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.keycol.User");
    	if (keycol == null || keycol.equals("")) {
    		keycol = ((TableName)Role.class.getAnnotation(TableName.class)).keycol();
    	}
    	String secretCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.secretCol.User");
    	if (secretCol == null || secretCol.equals("")) {
    		secretCol = "secret";
    	}

		User toReturn = new User();
		toReturn.setId(results.getString(keycol));
		toReturn.setSecret(results.getString(secretCol));
		return toReturn;
	}
}
