/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ActionBeanContext
{
	/**
	 * Gets the request the associated actionbean is handling.<br>
	 */
	public HttpServletRequest getRequest();
	
	/**
	 * Gets the response the associated actionbean is handling.<br>
	 */
	public HttpServletResponse getResponse();
	
	/**
	 * Gets the global ServletContext of the dispatcher.<br>
	 */
	public ServletContext getServletContext();
}
