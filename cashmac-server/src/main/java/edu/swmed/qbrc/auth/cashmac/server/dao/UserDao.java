package edu.swmed.qbrc.auth.cashmac.server.dao;

import java.sql.SQLException;
import java.sql.ResultSet;

import javax.servlet.ServletConfig;

import org.apache.commons.dbcp.BasicDataSource;

import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.data.User;

public class UserDao extends BaseDao<User> {
	
	private final RoleDao roleDao;
	
	@Inject
	public UserDao(RoleDao roleDao, final ServletConfig servletConfig, final BasicDataSource dataSource) {
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
		User toReturn = new User();
		toReturn.setId(results.getString("id"));
		toReturn.setPassword(results.getString("password"));
		toReturn.setSecret(results.getString("secret"));
		return toReturn;
	}
}
