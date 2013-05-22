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
import edu.swmed.qbrc.auth.cashmac.server.data.RoleUser;

public class RoleUserDao extends BaseDao<RoleUser> {
	
	@Inject
	public RoleUserDao(final Map<String, String> servletConfig, final BasicDataSource dataSource) {
		super(RoleUser.class, servletConfig, dataSource);
    }

    /* Load a role */
    public List<RoleUser> findByUsername(String username) throws SQLException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        List<RoleUser> results = new ArrayList<RoleUser>();

        // Get Context Parameters for Role table information
    	String table = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.RoleUsers");
    	if (table == null || table.equals("")) {
            table = ((TableName)RoleUser.class.getAnnotation(TableName.class)).value();
    	}
    	String usernameCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.username.RoleUsers");
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
	
    @Override
	public RoleUser setData(ResultSet results) throws SQLException {

        // Get Context Parameters for Role table information
    	String roleIdCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.roleid.RoleUsers");
    	if (roleIdCol == null || roleIdCol.equals("")) {
    		roleIdCol = ((TableName)RoleUser.class.getAnnotation(TableName.class)).keycol();
    	}
    	String usernameCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.username.RoleUsers");
    	if (usernameCol == null || usernameCol.equals("")) {
    		usernameCol = "role";
    	}

		
    	RoleUser toReturn = new RoleUser();
		toReturn.setRoleId(results.getInt(roleIdCol));
		toReturn.setUsername(results.getString(usernameCol));
		return toReturn;
	}
}
