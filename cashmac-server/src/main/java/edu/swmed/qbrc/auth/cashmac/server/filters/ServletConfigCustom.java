package edu.swmed.qbrc.auth.cashmac.server.filters;

import java.util.Enumeration;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletConfigCustom implements ServletConfig {

	private final FilterConfig config;
	
	ServletConfigCustom(FilterConfig config) {
		this.config = config;
	}
	
	@Override
	public String getInitParameter(String key) {
		return config.getInitParameter(key);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return config.getInitParameterNames();
	}

	@Override
	public ServletContext getServletContext() {
		return config.getServletContext();
	}

	@Override
	public String getServletName() {
		return "";
	}

}
