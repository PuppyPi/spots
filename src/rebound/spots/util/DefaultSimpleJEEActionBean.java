package rebound.spots.util;

import java.io.IOException;
import javax.servlet.ServletException;
import rebound.annotations.hints.ImplementationTransparency;
import rebound.simplejee.FlushPendingHttpServletResponseDecorator;
import rebound.simplejee.SimpleJEEUtilities;
import rebound.spots.ActionBean;
import rebound.spots.ActionBeanContext;

/**
 * This is guaranteed to use {@link #getRequest()} / {@link #getResponse()} not {@link #getContext()}.{@link ActionBeanContext#getRequest() getRequest()} / {@link #getContext()}.{@link ActionBeanContext#getResponse() getResponse()},
 * in case you want to override them to use wrappers (eg, {@link FlushPendingHttpServletResponseDecorator}).
 */
public interface DefaultSimpleJEEActionBean
extends ActionBean
{
	/**
	 * Finishes the web serving with a static page.
	 * 
	 * @param viewResourcePath  eg, "/styles/main.css"
	 */
	@ImplementationTransparency  //Java doesn't allow non-public methods in interfaces and doesn't allow multiple-inheritance in non-interfaces X'D
	public default void serveStaticView(String viewResourcePath) throws ServletException, IOException
	{
		SimpleJEEUtilities.serveStatically(getContext().getServletContext(), getRequest(), getResponse(), viewResourcePath);
	}
	
	
	
	/**
	 * Set a variable for the JSP view to use.
	 */
	@ImplementationTransparency  //Java doesn't allow non-public methods in interfaces and doesn't allow multiple-inheritance in non-interfaces X'D
	public default void setVariableForJSPView(String varname, Object value)
	{
		getRequest().setAttribute(varname, value);
	}
	
	/**
	 * Finishes the web serving with a static page.
	 * You probably want to call {@link #setVariableForJSPView(String, Object)} one or more times before this!
	 * 
	 * @param viewResourcePath  eg, "/forum/threads.jsp"
	 */
	@ImplementationTransparency  //Java doesn't allow non-public methods in interfaces and doesn't allow multiple-inheritance in non-interfaces X'D
	public default void serveJSPView(String viewResourcePath) throws ServletException, IOException
	{
		SimpleJEEUtilities.serveJSP(getContext().getServletContext(), getRequest(), getResponse(), viewResourcePath);
	}
}
