package edu.swmed.qbrc.auth.cashmac.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;
import edu.swmed.qbrc.auth.cashmac.server.data.ACL;
import edu.swmed.qbrc.auth.cashmac.server.data.RoleUser;

public class ACLDao extends BaseDao<ACL> {

	private static final Logger log = Logger.getLogger(ACLDao.class);
	
	private final String roleuserstable;
	private final String roleIdCol;
	private final String roleUsernameCol;
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


		// Get Context Parameters for Role Users table
    	String roleuserstable = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.RoleUsers");
    	if (roleuserstable == null || roleuserstable.equals("")) {
    		roleuserstable = ((TableName)RoleUser.class.getAnnotation(TableName.class)).value();
    	}
    	String roleIdCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.roleid.RoleUsers");
    	if (roleIdCol == null || roleIdCol.equals("")) {
    		roleIdCol = "roleid";
    	}
    	String roleUsernameCol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.username.RoleUsers");
    	if (roleUsernameCol == null || roleUsernameCol.equals("")) {
    		roleUsernameCol = "username";
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
    	this.roleUsernameCol = roleUsernameCol;
    	this.roleuserstable = roleuserstable;
    	this.roleIdCol = roleIdCol;
    	this.table = table;
    	this.keycol = keycol;
    	this.usernameCol = usernameCol;
    	this.roleCol = roleCol;
    	this.accessCol = accessCol;
    	this.classCol = classCol;
    	this.pkCol = pkCol;
    }

	/* Save an ACL */
	public void put(ACL acl) throws SQLException {
		
        PreparedStatement stmt = null;
        Connection conn = null;
    	
        try {

            // Get connection
            conn = dataSource.getConnection();

            // Prepare query
            String sqlstmt = 
            		"INSERT INTO " + table + " (" +
            			usernameCol + ", " +
            		    roleCol + ", " +
            			accessCol + ", " +
            		    classCol + ", " +
            			pkCol +
            		") VALUES (?, ?, ?, ?, ?)";
            		
            stmt = conn.prepareStatement(sqlstmt);
            if (acl.getUsername() != null)
            	stmt.setString(1, acl.getUsername());
            else
            	stmt.setNull(1, java.sql.Types.VARCHAR);
            if (acl.getRoleId() != null)
            	stmt.setInt   (2, acl.getRoleId());
            else
            	stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.setString(3, acl.getAccess());
            stmt.setString(4, acl.getObjectClass());
            stmt.setObject(5, acl.getObjectPK());

            // Execute query
           	log.trace(sqlstmt + "[user=" + acl.getUsername() + ";role=" + acl.getRoleId() + ";access=" + acl.getAccess() + ";class=" + acl.getObjectClass() + ";pk=" + acl.getObjectPK() + "]");
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Database Error: " + e.getMessage());
        } catch(Exception e) {
            throw new RuntimeException("Exception: " + e.getMessage());
        } finally {
            if (stmt != null) {
                try {stmt.close(); } catch (SQLException e) { throw e; }
            }
            if (conn != null) {
                try {conn.close(); } catch (SQLException e) { throw e; }
            }
        }
	}
	
    /* Load an ACL */
    public List<ACL> findAcl(String username, String accessLevel, Class<?> objectClass, Object key) throws SQLException {

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
            					"SELECT " + roleIdCol + " FROM " + roleuserstable + " " +
            					"WHERE " + roleUsernameCol + " = ? " + 
            				") " +
            			") " +
            			"AND " + classCol  + " = ? " +
            			"AND " + pkCol     + " = ? " +
            			"AND " + accessCol + " = ? ";
            stmt = conn.prepareStatement(sqlstmt);
            
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, objectClass.getName());
           	//stmt.setObject(4, key);
           	stmt.setString(4, key.toString());
            stmt.setString(5, accessLevel);

            // Execute query
            rs = stmt.executeQuery();
           	log.trace(rs.getStatement());

            // If an ACL was found, load it.
            while (rs.next()) {
            	results.add(setData(rs));
            }

        } catch (SQLException e) {
        	System.out.println("------------------------------------------------");
        	e.printStackTrace();
        	System.out.println("------------------------------------------------");
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
	
    /* Load ACLs (but don't consider user/role) */
    public List<ACL> findAclNonUserSpecific(String accessLevel, Class<?> objectClass, Object key) throws SQLException {

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
            		"WHERE 1=1 " +
            			"AND " + classCol  + " = ? " +
            			"AND " + pkCol     + " = ? " +
            			"AND " + accessCol + " = ? ";
            stmt = conn.prepareStatement(sqlstmt);
            
            stmt.setString(1, objectClass.getName());
            stmt.setObject(2, key);
            stmt.setString(3, accessLevel);

            // Execute query
            rs = stmt.executeQuery();
           	log.trace(rs.getStatement());

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

    /* Load an ACL */
    public List<ACL> findObjectAcls(Class<?> objectClass, Object key) throws SQLException {

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
            			"1=1 " +
            			"AND " + classCol  + " = ? " +
            			"AND " + pkCol     + " = ? ";
            stmt = conn.prepareStatement(sqlstmt);
            
            stmt.setString(1, objectClass.getName());
            stmt.setObject(2, key);

            // Execute query
            rs = stmt.executeQuery();
            log.trace(rs.getStatement());

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
