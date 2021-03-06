package edu.swmed.qbrc.auth.cashmac.server.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CasHmacRequestFilter implements Filter {

	private static ThreadLocal<HttpServletRequest> localRequest = new ThreadLocal<HttpServletRequest>();
	private static ThreadLocal<HttpServletResponse> localResponse = new ThreadLocal<HttpServletResponse>();
	private static Map<String, String> servletConfig = new HashMap<String, String>();
	
	public static Map<String, String> getConfig() {
		return servletConfig;
	}
	
	public static HttpServletRequest getRequest() {
		return localRequest.get();
	}
	
	public static HttpServletResponse getResponse() {
		return localResponse.get();
	}
	
	public static HttpSession getSession() {
		HttpServletRequest request = localRequest.get();
		return (request != null) ? request.getSession() : null;
	}
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		if (servletRequest instanceof HttpServletRequest) {
			localRequest.set((HttpServletRequest) servletRequest);
		}
		if (servletResponse instanceof HttpServletResponse) {
			localResponse.set((HttpServletResponse) servletResponse);
		}
		
		try {
			filterChain.doFilter(servletRequest, servletResponse);
		}
		finally {
			localRequest.remove();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		Enumeration<?> enumer = config.getInitParameterNames();
		while (enumer.hasMoreElements()) {
			String key = (String)enumer.nextElement();
			servletConfig.put(key, config.getInitParameter(key));
		}
	}
	
}