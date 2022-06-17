/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractHTTPFilter
implements Filter
{
	protected FilterConfig config;
	
	public AbstractHTTPFilter()
	{
		super();
	}
	
	
	
	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.config = filterConfig;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse)
			doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
		else
			chain.doFilter(request, response);
	}
	
	public void destroy()
	{
	}
	
	
	
	public abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;
	
	
	
	public FilterConfig getFilterConfig()
	{
		return this.config;
	}
	
	public ServletContext getServletContext()
	{
		return getFilterConfig().getServletContext();
	}
	
	public void log(String msg)
	{
		getServletContext().log(getClass()+": "+msg);
	}
	
	public void log(String msg, Throwable t)
	{
		getServletContext().log(getClass()+": "+msg, t);
	}
}
