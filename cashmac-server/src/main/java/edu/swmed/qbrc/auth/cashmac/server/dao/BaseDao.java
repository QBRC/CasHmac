package edu.swmed.qbrc.auth.cashmac.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;
import com.google.inject.Inject;
import edu.swmed.qbrc.auth.cashmac.server.dao.annotations.TableName;
import edu.swmed.qbrc.auth.cashmac.server.data.BaseEntity;

public abstract class BaseDao<T extends BaseEntity> {    
    private final Class<T> clazz;

    final Map<String, String> servletConfig;
    final BasicDataSource dataSource;
    
    @Inject
    public BaseDao(final Class<T> clazz, final Map<String, String> servletConfig, final BasicDataSource dataSource) { 
        this.clazz = clazz;
        this.servletConfig = servletConfig;
        this.dataSource = dataSource;
    }

    public abstract T setData(ResultSet results) throws SQLException;
    
    public T load(Object id) throws SQLException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        T result = null;
        
        // Get Context Parameters for table information
    	String table = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table." + clazz.getSimpleName());
    	if (table == null || table.equals("")) {
    		table = ((TableName)clazz.getAnnotation(TableName.class)).value();
    	}
    	String keycol = servletConfig.get("edu.swmed.qbrc.auth.cashmac.hmac.table.keycol." + clazz.getSimpleName());
    	if (keycol == null || keycol.equals("")) {
    		keycol = ((TableName)clazz.getAnnotation(TableName.class)).keycol();
    	}

        try {

            // Get connection
            conn = dataSource.getConnection();

            // Prepare query
            String sql = "select * from " + table + " where " + keycol + " = ?";
            //System.out.println("BaseDao Statement: " + sql);
            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id);

            // Execute query
            rs = stmt.executeQuery();

            // If an item was found, load it.
            if (rs.next()) {
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
    
}
