package rebound.spots.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.spots.ActionBeanContext;

public class SimpleImmutableActionBeanContext
implements ActionBeanContext
{
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	protected final ServletContext servletContext;
	
	public SimpleImmutableActionBeanContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext)
	{
		this.request = request;
		this.response = response;
		this.servletContext = servletContext;
	}
	
	
	public HttpServletRequest getRequest()
	{
		return request;
	}
	
	public HttpServletResponse getResponse()
	{
		return response;
	}
	
	public ServletContext getServletContext()
	{
		return servletContext;
	}
}
//Beany>