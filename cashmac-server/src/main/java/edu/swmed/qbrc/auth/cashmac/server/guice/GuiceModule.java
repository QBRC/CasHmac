package edu.swmed.qbrc.auth.cashmac.server.guice;

import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletConfig;
import org.apache.commons.dbcp.BasicDataSource;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class GuiceModule extends AbstractModule {
	
	private final ServletConfig servletConfig;
	
	public GuiceModule(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}
	
	@Override
	protected void configure() {
		//JpaPersistModule jpa = new JpaPersistModule(getPersistenceUnit());
		//install(new JpaPersistModule(getPersistenceUnit()));
		Names.bindProperties(binder(), loadProperties());
		bind(BasicDataSource.class).toProvider(DBConnectionPool.class).in(Scopes.SINGLETON);
	}

	@Provides
	ServletConfig provideServletConfig() {
		return this.servletConfig;
	}
	
	static class DBConnectionPool implements Provider<BasicDataSource> {

		private final BasicDataSource cpds;

		@Inject
		public DBConnectionPool(@Named("driver") final String driver,
								@Named("url") final String url,
								@Named("user") final String user,
								@Named("password") final String password) throws SQLException {
			
        	cpds = new BasicDataSource();
	        try{
				cpds.setDriverClassName(driver);
	        	cpds.setUrl(url);
	        	cpds.setUsername(user);
	        	cpds.setPassword(password);
	        	cpds.setInitialSize(1);
	        	cpds.setMaxActive(10);
	        } catch (Exception e) {
	            System.err.println("Problem setting up connection pool: " + e);
	        }
			
		}

		public BasicDataSource get() {
			return cpds;
		}
		
	}
	
	private Properties loadProperties() {
    	// Get context parameters for JDBC connection information
    	String driver = servletConfig.getServletContext().getInitParameter("edu.swmed.qbrc.auth.cashmac.hmac.jdbcdriver");
    	if (driver == null || driver.equals("")) {
    		driver = "org.h2.Driver";
    	}
    	String url = servletConfig.getServletContext().getInitParameter("edu.swmed.qbrc.auth.cashmac.hmac.jdbcurl");
    	if (url == null || url.equals("")) {
    		url = "jdbc:h2:~/cashmacSampleServerDB";
    	}
    	String user = servletConfig.getServletContext().getInitParameter("edu.swmed.qbrc.auth.cashmac.hmac.username");
    	if (user == null || user.equals("")) {
    		user = "sa";
    	}
    	String password = servletConfig.getServletContext().getInitParameter("edu.swmed.qbrc.auth.cashmac.hmac.password");
    	if (password == null || password.equals("")) {
    		password = "";
    	}
    	
    	Properties props = new Properties();
    	props.setProperty("driver", driver);
    	props.setProperty("url", url);
    	props.setProperty("user", user);
    	props.setProperty("password", password);
    	
    	return props;

	}
	
	
}