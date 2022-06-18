/*
 * Created on May 28, 2007
 * 	by the great Eclipse(c)
 */
package rebound.simplejee;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This is a deprecated class which must be used to wrap other requests because Tomcat has an unfathomable love for it.<br>
 * Todo Well then what *should* we do, past-me? XD
 * 
 * @author RProgrammer
 */
public class HttpServletRequestWithNewURIDecorator
extends HttpServletRequestWrapper
{
	protected String newURI;
	
	
	public HttpServletRequestWithNewURIDecorator(HttpServletRequest request, String newURI)
	{
		super(request);
		this.newURI = newURI;
	}
	
	@Override
	public HttpServletRequest getRequest()
	{
		return (HttpServletRequest)super.getRequest();
	}
	
	
	@Override
	public String getRequestURI()
	{
		return newURI;
	}
	
	@Override
	public String getPathInfo()
	{
		return newURI;
	}
	
	@Override
	public String getServletPath()
	{
		return "";
	}
}
