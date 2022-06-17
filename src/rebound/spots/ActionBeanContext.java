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
	
	/**
	 * Gets the names of all parameters specific to this type of action bean (they are not specific to this instance, though).<br>
	 */
	public String[] getInitParameterNames();
	
	/**
	 * Gets a parameter specific to this type of action bean (they are not specific to this instance, though) or <code>null</code> if one could not be found.<br>
	 */
	public String getInitParameter(String name);
	
	/**
	 * Gets the names of all parameters found by the dispatcher.<br>
	 */
	public String[] getGlobalInitParameterNames();
	
	/**
	 * Gets a parameter found by the dispatcher or <code>null</code> if one could not be found.<br>
	 */
	public String getGlobalInitParameter(String name);
	
	
	/**
	 * This gets a {@link ActionBeanContext#getInitParameter(String) actionbean class - specific} parameter.<br>
	 * But if one cannot be found, returns a {@link ActionBeanContext#getGlobalInitParameter(String) global init parameter}.<br>
	 * If <i>that</i> cannot be found, returns <code>null</code>.<br>
	 */
	public String getAParameter(String name);
}
