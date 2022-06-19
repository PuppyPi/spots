package rebound.simplejee;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.file.FSUtilities;

public class SimpleJEEUtilities
{
	public static final String DefaultServletNameForStaticServing = "default";
	public static final String DefaultServletNameForJSP = "jsp";
	
	
	
	/**
	 * @param context  eg, {@link ServletConfig#getServletContext()}
	 * @param request  eg, from {@link Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
	 * @param response  eg, from {@link Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
	 * @param relativePathInWebappToStaticFileStartingWithSlash  eg, "/styles/main.css"
	 */
	public static void serveStatically(ServletContext context, HttpServletRequest request, HttpServletResponse response, String relativePathInWebappToStaticFileStartingWithSlash) throws ServletException, IOException
	{
		serveStatically(context, request, response, relativePathInWebappToStaticFileStartingWithSlash, DefaultServletNameForStaticServing);
	}
	
	public static void serveStatically(ServletContext context, HttpServletRequest request, HttpServletResponse response, String relativePathInWebappToStaticFileStartingWithSlash, String staticServletName) throws ServletException, IOException
	{
		if (FSUtilities.doesPathContainStandardUpwardTraversalMetaElement(relativePathInWebappToStaticFileStartingWithSlash))
			throw new SecurityException("Ascending relative path given in DEFAULT-servlet (static pages) forward URI: \""+relativePathInWebappToStaticFileStartingWithSlash+"\"");
		
		HttpServletRequestWithNewURIDecorator newRequest = new HttpServletRequestWithNewURIDecorator(request, relativePathInWebappToStaticFileStartingWithSlash);
		context.getNamedDispatcher(staticServletName).forward(newRequest, response);
	}
	
	
	
	
	/**
	 * @param context  eg, {@link ServletConfig#getServletContext()}
	 * @param request  eg, from {@link Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
	 * @param response  eg, from {@link Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
	 * @param relativePathInWebappToJSPFileStartingWithSlash  eg, "/index.jsp" or "/forum/threads.jsp" or "/errors/notfound.jsp" or etc. :3
	 */
	public static void serveJSP(ServletContext context, HttpServletRequest request, HttpServletResponse response, String relativePathInWebappToJSPFileStartingWithSlash) throws ServletException, IOException
	{
		serveJSP(context, request, response, relativePathInWebappToJSPFileStartingWithSlash, DefaultServletNameForJSP);
	}
	
	public static void serveJSP(ServletContext context, HttpServletRequest request, HttpServletResponse response, String relativePathInWebappToJSPFileStartingWithSlash, String jspServletName) throws ServletException, IOException
	{
		if (FSUtilities.doesPathContainStandardUpwardTraversalMetaElement(relativePathInWebappToJSPFileStartingWithSlash))
			throw new SecurityException("Ascending relative path given in JSP forward URI: \""+relativePathInWebappToJSPFileStartingWithSlash+"\"");
		
		HttpServletRequestWithNewURIDecorator newRequest = new HttpServletRequestWithNewURIDecorator(request, relativePathInWebappToJSPFileStartingWithSlash);
		context.getNamedDispatcher(jspServletName).forward(newRequest, response);
	}
}
