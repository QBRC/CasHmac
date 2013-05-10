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
import edu.swmed.qbrc.auth.cashmac.server.data.ACL;
import edu.swmed.qbrc.auth.cashmac.server.data.Role;

public class ACLDao extends BaseDao<ACL> {

	private static Boolean DEBUG = true;
	
	private final String roletable;
	private final String roleUsernameCol;
	private final String roleKeyCol;
	private final String table;
	private final String keycol;
	private final String usernameCol;
	private final String roleCol;
	private final String accessCol;
	private final String classCol;
	private final String pkCol;
	
	@Inject
	public ACLDao(final Map<String, String> servletConfig, final BasicDataSource dataSource) {
		super(ACL.class, servletConfig, dataSource);

		// Get Context Parameters for Role table
    	String roletable = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.Role");
    	if (roletable == null || roletable.equals("")) {
    		roletable = ((TableName)Role.class.getAnnotation(TableName.class)).value();
    	}
    	String roleUsernameCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.usercol.Role");
    	if (roleUsernameCol == null || roleUsernameCol.equals("")) {
    		roleUsernameCol = "username";
    	}
    	String roleKeyCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.keycol.Role");
    	if (roleKeyCol == null || roleKeyCol.equals("")) {
    		roleKeyCol = "id";
    	}

		
		// Get Context Parameters for ACL table information
    	String table = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.ACL");
    	if (table == null || table.equals("")) {
            table = ((TableName)ACL.class.getAnnotation(TableName.class)).value();
    	}
    	String keycol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.keycol.ACL");
    	if (keycol == null || keycol.equals("")) {
    		keycol = ((TableName)ACL.class.getAnnotation(TableName.class)).keycol();
    	}
    	String usernameCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.usercol.ACL");
    	if (usernameCol == null || usernameCol.equals("")) {
    		usernameCol = "username";
    	}
    	String roleCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.rolecol.ACL");
    	if (roleCol == null || roleCol.equals("")) {
    		roleCol = "role_id";
    	}
    	String accessCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.accesscol.ACL");
    	if (accessCol == null || accessCol.equals("")) {
    		accessCol = "access";
    	}
    	String classCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.classcol.ACL");
    	if (classCol == null || classCol.equals("")) {
    		classCol = "class";
    	}
    	String pkCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.pkcol.ACL");
    	if (pkCol == null || pkCol.equals("")) {
    		pkCol = "pk";
    	}

    	// Initialize fields with properties from .properties file.
    	this.roletable = roletable;
    	this.roleUsernameCol = roleUsernameCol;
    	this.roleKeyCol = roleKeyCol;
    	this.table = table;
    	this.keycol = keycol;
    	this.usernameCol = usernameCol;
    	this.roleCol = roleCol;
    	this.accessCol = accessCol;
    	this.classCol = classCol;
    	this.pkCol = pkCol;
    }

    /* Load an ACL */
    public List<ACL> findAcl(String username, String accessLevel, Class<?> objectClass, String key) throws SQLException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        List<ACL> results = new ArrayList<ACL>();
        
    	
        try {

            // Get connection
            conn = dataSource.getConnection();

            // Prepare query
            String sqlstmt = 
            		"SELECT * " +
            		"FROM " + table + " " +
            		"WHERE " +
            			"( 1=0 " +
            				"OR " + usernameCol + " = ? " +
            				"OR " + roleCol     + " IN ( " +
            					"SELECT " + roleKeyCol + " FROM " + roletable + " " +
            					"WHERE " + roleUsernameCol + " = ? " + 
            				") " +
            			") " +
            			"AND " + classCol  + " = ? " +
            			"AND " + pkCol     + " = ? " +
            			"AND " + accessCol + " = ? ";
            stmt = conn.prepareStatement(sqlstmt);
            
            stmt.setString(1, username);
            stmt.setObject(2, username);
            stmt.setString(3, objectClass.getName());
            stmt.setObject(4, key);
            stmt.setString(5, accessLevel);

            // Execute query
            rs = stmt.executeQuery();
            if (DEBUG) 
            	System.out.println(rs.getStatement());

            // If an ACL was found, load it.
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
	public ACL setData(ResultSet results) throws SQLException {
		ACL toReturn = new ACL();
		toReturn.setId(results.getInt(keycol));
		toReturn.setUsername(results.getString(usernameCol));
		toReturn.setRoleId(results.getInt(roleCol));
		toReturn.setAccess(results.getString(accessCol));
		toReturn.setObjectClass(results.getString(classCol));
		toReturn.setObjectPK(results.getString(pkCol));
		return toReturn;
	}
}
