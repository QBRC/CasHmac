package edu.swmed.qbrc.auth.cashmac.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;
import edu.swmed.qbrc.auth.cashmac.server.data.Role;

public class RoleDao extends BaseDao<Role> {
	
	@Inject
	public RoleDao(final Map<String, String> servletConfig, final BasicDataSource dataSource) {
		super(Role.class, servletConfig, dataSource);
    }

    /* Load a role */
    public List<Role> findByUsername(String username) throws SQLException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        List<Role> results = new ArrayList<Role>();
        
        // Get Context Parameters for Role table information
    	String table = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table." + Role.class.getSimpleName());
    	if (table == null || table.equals("")) {
            table = ((TableName)Role.class.getAnnotation(TableName.class)).value();
    	}
    	String usernameCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.usercol." + Role.class.getSimpleName());
    	if (usernameCol == null || usernameCol.equals("")) {
    		usernameCol = "username";
    	}

        try {

            // Get connection
            conn = dataSource.getConnection();

            // Prepare query
            stmt = conn.prepareStatement("select * from " + table + " where " + usernameCol + " = ?");
            stmt.setString(1, username);

            // Execute query
            rs = stmt.executeQuery();

            // If roles were found, load them.
            while (rs.next()) {
            	results.add(setData(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database Error: " + e.getMessage());
        } catch(Exception e) {
            throw new RuntimeException("Exception: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {rs.close(); } catch (SQLException e) { throw e; }
            }
            if (stmt != null) {
                try {stmt.close(); } catch (SQLException e) { throw e; }
            }
            if (conn != null) {
                try {conn.close(); } catch (SQLException e) { throw e; }
            }
        }

        return results;
    }    
	
    /* Load a role */
    public Role findByRoleName(String rolename) throws SQLException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        Role result = null;
        
        // Get Context Parameters for Role table information
    	String table = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.Role");
    	if (table == null || table.equals("")) {
            table = ((TableName)Role.class.getAnnotation(TableName.class)).value();
    	}
    	String roleCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.rolecol.Role");
    	if (roleCol == null || roleCol.equals("")) {
    		roleCol = "role";
    	}

        try {

            // Get connection
            conn = dataSource.getConnection();

            // Prepare query
            stmt = conn.prepareStatement("select * from " + table + " where " + roleCol + " = ?");
            stmt.setString(1, rolename);

            // Execute query
            rs = stmt.executeQuery();

            // If a role was found, load it.
            while (rs.next()) {
            	result = setData(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database Error: " + e.getMessage());
        } catch(Exception e) {
            throw new RuntimeException("Exception: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {rs.close(); } catch (SQLException e) { throw e; }
            }
            if (stmt != null) {
                try {stmt.close(); } catch (SQLException e) { throw e; }
            }
            if (conn != null) {
                try {conn.close(); } catch (SQLException e) { throw e; }
            }
        }

        return result;
    }    

    @Override
	public Role setData(ResultSet results) throws SQLException {

        // Get Context Parameters for Role table information
    	String keycol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.keycol.Role");
    	if (keycol == null || keycol.equals("")) {
    		keycol = ((TableName)Role.class.getAnnotation(TableName.class)).keycol();
    	}
    	String usernameCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.usercol.Role");
    	if (usernameCol == null || usernameCol.equals("")) {
    		usernameCol = "username";
    	}
    	String roleCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.rolecol.Role");
    	if (roleCol == null || roleCol.equals("")) {
    		roleCol = "role";
    	}

		
		Role toReturn = new Role();
		toReturn.setId(results.getInt(keycol));
		toReturn.setUsername(results.getString(usernameCol));
		toReturn.setRole(results.getString(roleCol));
		return toReturn;
	}
}
