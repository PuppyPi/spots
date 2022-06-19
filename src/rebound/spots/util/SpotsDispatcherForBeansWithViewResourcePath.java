package rebound.spots.util;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.simplejee.AbstractHttpServlet;
import rebound.simplejee.SimpleJEEUtilities;
import rebound.spots.SpotsDispatcher;
import rebound.util.collections.PairOrdered;

/**
 * This is a convenience around {@link SpotsDispatcher} and replacement for {@link SpotsDispatcher#dispatch(ServletContext, HttpServletRequest, HttpServletResponse, Class, boolean)} for the most common kind of web servers (ones that still use .jsp/.html/.css/.js/etc. files of some kind which are organized in a directory structure similarly to how the URL/URI is!).
 */
public class SpotsDispatcherForBeansWithViewResourcePath
{
	/**
	 * You might want to not just pass {@link SpotsDispatcher#getActionBeanClass(String, Class)} or similar and the view resource path into here in your {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)} implementation, but consider making your own <code>public static {@link PairOrdered}<Class, String> getActionBeanClassAndViewResourcePathname(String requestURIPath)</code> method or similar.
	 * (especially if your URLs can have database-based non-static parts!) so that other things in the system can check URLs (really URI path parts) to see if they're servable by your webapp!  (eg, when selecting a name for the dynamic user-generated part to tell if it overlaps with a page! like if they try making their username be "about" or "robots.txt" XD )
	 * 
	 * This method is actually rarely used and serves as just an example for all but the simplest of webservers/webapps.
	 * 
	 * @param servletContext  usually from {@link Servlet#getServletConfig()}.{@link ServletConfig#getServletContext()}
	 * @param request  usually from {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)}
	 * @param response  usually from {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)}
	 * @param actionBeanClass  usually from {@link SpotsDispatcher#getActionBeanClass(String, Class)} or similar (which, itself, pulls from {@link SpotsDispatcher#getActionBeanClassName(String, String, String)} or similar)
	 * @param viewResourcePath  the default resource pathname that *would* be used (whether it is or not) by the Action Bean for {@link SimpleJEEUtilities#serveStatically(ServletContext, HttpServletRequest, HttpServletResponse, String)} or {@link SimpleJEEUtilities#serveJSP(ServletContext, HttpServletRequest, HttpServletResponse, String)} :>
	 * @param verbose  log even non error hits to {@link ServletContext#log(String)} ?
	 */
	public static void dispatch(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, Class<? extends ActionBeanWithViewResourcePath> actionBeanClass, String viewResourcePath, boolean verbose) throws ServletException, IOException
	{
		//Try to instantiate and run an actionbean
		//Don't pay any attention to the HTTP METHOD, the beans take care of that.
		
		
		ActionBeanWithViewResourcePath bean = SpotsDispatcher.newActionBean(actionBeanClass, servletContext, request, response);
		
		//<Use bean
		if (bean == null)
		{
			servletContext.log("Error instantiating "+(actionBeanClass == null ? null : actionBeanClass.getName())+" for "+request.getRequestURI());
			String msg = "Error getting actionbean "+(actionBeanClass == null ? null : actionBeanClass.getName());
			response.sendError(500, msg);
		}
		else
		{
			if (verbose)
				servletContext.log("Invoking "+actionBeanClass.getName()+" for "+request.getRequestURI());
			
			bean.setViewResourcePath(viewResourcePath);  //the critical difference from SpotsDispatcher X3
			
			bean.doAction();
		}
		//Use bean>
	}
}
