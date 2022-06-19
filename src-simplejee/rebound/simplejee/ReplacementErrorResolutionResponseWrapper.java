package rebound.simplejee;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ReplacementErrorResolutionResponseWrapper
extends HttpServletResponseWrapper
{
	protected final HttpServletRequest request;
	protected final ServletContext servletContext;
	protected final SimpleJEEErrorHandler handler;
	
	public ReplacementErrorResolutionResponseWrapper(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, SimpleJEEErrorHandler handler)
	{
		super(response);
		this.request = request;
		this.servletContext = servletContext;
		this.handler = handler;
	}
	
	
	
	
	
	//<Actual Overrides
	@Override
	public void sendError(int code) throws IOException
	{
		if (!handler.sendError(getRequest(), this, getServletContext(), code, null))
			super.sendError(code);
	}
	
	@Override
	public void sendError(int code, String msg) throws IOException
	{
		if (!handler.sendError(getRequest(), this, getServletContext(), code, msg))
			super.sendError(code, msg);
	}
	//Actual Overrides>
	
	
	
	
	
	
	public HttpServletRequest getRequest()
	{
		return this.request;
	}
	
	public ServletContext getServletContext()
	{
		return servletContext;
	}
}