package rebound.spots;

import static rebound.net.NetworkUtilities.*;
import static rebound.text.StringUtilities.*;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.simplejee.AbstractHttpServlet;
import rebound.simplejee.SimpleJEEUtilities;
import rebound.spots.util.SimpleImmutableActionBeanContext;
import rebound.util.collections.PairOrdered;
import rebound.util.functional.FunctionInterfaces.UnaryProcedure;

/**
 * This is the heart and soul of Spots!
 * Honestly, this *is* Spots! XD
 * Everything else (namely the annotation-based form parameter decoding and binding) is just optional niceties :3
 * 
 * This is the core of the Spots Philosophy for web servers XD
 * Ie, that each individual request be handled by a separate Java object (an {@link ActionBean})!
 * (as opposed to {@link Servlet}s, where one object handles many requests (usually involving multithreading o,o ))
 * 
 * You can use a stock servlet (like in Spots/TraditionalJEE), or write your own (the SimpleJEE way!).
 * But either way, this class stores all the important (and security-risky-to-edit) code as just normal code, no web.xml-related things or loosey-goosey {@link String}-based "attributes" and "parameters" passing Java objects between code here! :D
 */
public class SpotsDispatcher
{
	/**
	 * You might want to not just pass {@link #getActionBeanClass(String, Class)} or similar into here in your {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)} implementation, but consider making your own getActionBeanClass(String) (especially if your URLs can have database-based non-static parts!) so that other things in the system can check URLs (really URI path parts) to see if they're servable by your webapp!  (eg, when selecting a name for the dynamic user-generated part to tell if it overlaps with a page! like if they try making their username be "about" or "robots.txt" XD )
	 * Or better, usually, write a method <code>public static {@link PairOrdered}<Class, String> getActionBeanClassAndViewResourcePathname(String requestURIPath)</code> or similar, which also provides the default resource pathname that *would* be used (whether it is or not) by the Action Bean for {@link SimpleJEEUtilities#serveStatically(ServletContext, HttpServletRequest, HttpServletResponse, String)} or {@link SimpleJEEUtilities#serveJSP(ServletContext, HttpServletRequest, HttpServletResponse, String)} :>
	 * 
	 * @param servletContext  usually from {@link Servlet#getServletConfig()}.{@link ServletConfig#getServletContext()}
	 * @param request  usually from {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)}
	 * @param response  usually from {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)}
	 * @param initializeActionBean  set things like the viewResourcePath if your system uses that, and/or database connections, and/or caches, and/or alllllll the things that would otherwise need to be passed around through static variables in Traditional JEE that can be done in the Java way in Simple JEE! :D  (or just no-op if you want, that's fine too XD )
	 * @param actionBeanClass  usually from {@link #getActionBeanClass(String, Class)} or similar (which, itself, pulls from {@link #getActionBeanClassName(String, String, String)} or similar)
	 * @param verbose  log even non error hits to {@link ServletContext#log(String)} ?
	 */
	public static <T extends ActionBean> void dispatch(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, Class<T> actionBeanClass, @Nonnull UnaryProcedure<? super T> initializeActionBean, boolean verbose) throws ServletException, IOException
	{
		//Try to instantiate and run an actionbean
		//Don't pay any attention to the HTTP METHOD, the beans take care of that.
		
		
		T bean = newActionBean(actionBeanClass, servletContext, request, response);
		
		
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
			
			initializeActionBean.f(bean);
			
			bean.doAction();
		}
		//Use bean>
	}
	
	
	
	
	
	
	/**
	 * The Java Fully-Qualified Class Name of the action bean:
	 * 
	 * eg, (if prefix = "com.example.webui.actions." and suffix = "Action" (for demonstration. probably you don't want a suffix but who knows!) ),
	 * 
	 * "/" → "com.example.webui.actions.Action"
	 * "/index" → "com.example.webui.actions.indexAction"
	 * "/index.html" → "com.example.webui.actions.index_htmlAction"
	 * "/index.jsp" → "com.example.webui.actions.index_jspAction"
	 * "/index.css" → "com.example.webui.actions.index_cssAction"
	 * "/index_css" → "com.example.webui.actions.index_cssAction"  (a noninjectivity!)
	 * "/about" → "com.example.webui.actions.aboutAction"
	 * "/About" → "com.example.webui.actions.AboutAction"
	 * "/about/" → "com.example.webui.actions.aboutAction"
	 * "/about/us" → "com.example.webui.actions.about.usAction"
	 * "/about-us" → "com.example.webui.actions.about_usAction"
	 * "/about_us" → "com.example.webui.actions.about_usAction"  (a noninjectivity!)
	 * "/about.html" → "com.example.webui.actions.about_htmlAction"
	 * "/about%2Ehtml" → "com.example.webui.actions.about_htmlAction"
	 * "/./about%2Ehtml" → "com.example.webui.actions._.about_htmlAction"  (enforces simple URI usage to aid security)
	 * "/WEB-INF/web.xml" → "com.example.webui.actions.WEB_INF.web_xmlAction"
	 * "/../../conf/server.xml" → "com.example.webui.actions.__.__.conf.server_xmlAction"  (enforces simple URI usage to aid security)
	 * "/%2E%2E/%2E%2E/conf/server.xml" → "com.example.webui.actions.__.__.conf.server_xmlAction"
	 * 
	 * "/λ" → "com.example.webui.actions.λAction"
	 * "/Λ" → "com.example.webui.actions.λAction"
	 * "/%CE%BB" → "com.example.webui.actions.λAction"
	 * "/πληροφορίες" → "com.example.webui.actions.πληροφορίεςAction"
	 * "/Πληροφορίες" → "com.example.webui.actions.ΠληροφορίεςAction"
	 * "/%cf%80%ce%bb%ce%b7%cf%81%ce%bf%cf%86%ce%bf%cf%81%ce%af%ce%b5%cf%82" → "com.example.webui.actions.πληροφορίεςAction"
	 * 
	 * (these are all indeed valid Java class names!)
	 * (but if you were actually using a language other than English you probably wouldn't make "Action" be the suffix XD )
	 * 
	 * @param requestURIPath  the path like {@link HttpServletRequest#getRequestURI()} would return (ie, starting with a slash and without a query string or anchor fragment!)
	 * @param actionBeansPrefix  eg, "com.example.webui.actions."
	 * @param actionBeansSuffix  eg, "Action"
	 * @return null on syntax error (eg, doesn't start with "/")
	 */
	public static @Nullable String getActionBeanClassName(@Nonnull String requestURIPath, String actionBeansPrefix, String actionBeansSuffix)
	{
		//We use Unmodified by default to be consistent with most of the web, wherein URL/URI paths are totally case-sensitive
		return getActionBeanClassName(requestURIPath, actionBeansPrefix, actionBeansSuffix, '_', '_');
	}
	
	
	/**
	 * @param charForDots  if null, then consider dots to be unmappable (and return null if any are present)
	 * @param charForDashes  if null, then consider dots to be unmappable (and return null if any are present)
	 */
	public static @Nullable String getActionBeanClassName(@Nonnull String requestURIPath, String actionBeansPrefix, String actionBeansSuffix, Character charForDots, Character charForDashes)
	{
		if (!requestURIPath.startsWith("/"))
			return null;
		
		requestURIPath = trimstr(requestURIPath, "/");  //Trim the initial slash and the optional trailing slash
		
		//The stem section is between the prefix and the suffix
		String actionBeanStem;
		{
			//It has the uri-dots (.css, .html, ...) replaced with underscores or something and its uri-slashes replaced with package-dots
			String[] pathElements = split(requestURIPath, '/');
			
			int n = pathElements.length;
			
			for (int i = 0; i < n; i++)
			{
				String pathElement = pathElements[i];
				
				pathElement = urldescape(pathElement);
				
				//Some standard-to-Spots conversions for common characters that are legal in URLs, but illegal in Java identifiers
				
				//NOTE that dots are converted *after* url descaping!!
				//This way, if %2E appears, it won't allow an attacker to get to a (sub)package you might not expect them to!
				
				if (charForDots != null)
					pathElement = pathElement.replace('.', charForDots);
				else if (contains(pathElement, '.'))
					return null;
				
				if (charForDashes != null)
					pathElement = pathElement.replace('-', charForDashes);
				else if (contains(pathElement, '-'))
					return null;
				
				pathElements[i] = pathElement;
			}
			
			actionBeanStem = joinStrings('.', pathElements);  //Convert to a package
		}
		
		
		//Attach the prefix+stem+suffix
		String actionBeanClassName = actionBeansPrefix + actionBeanStem + actionBeansSuffix;
		
		
		return actionBeanClassName;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @param actionBeanClassName  straight from {@link #getActionBeanClassName(String, String, String)}
	 * @param defaultActionBeanClass  for 404's and otherwise invalid URI paths
	 */
	public static Class getActionBeanClass(@Nullable String actionBeanClassName, Class defaultActionBeanClass)
	{
		return getActionBeanClass(actionBeanClassName, defaultActionBeanClass, defaultActionBeanClass);
	}
	
	/**
	 * @param actionBeanClassName  straight from {@link #getActionBeanClassName(String, String, String)}
	 * @param defaultActionBeanClassForUriSyntaxError  for whenever {@link #getActionBeanClassName(String, String, String)} returns null (eg, a uri path without a leading "/")
	 * @param defaultActionBeanClassForNotFounds  for whenever it could be a valid action bean class..it just isn't XD  (this is what happens on almost every 404!)
	 */
	public static Class getActionBeanClass(@Nullable String actionBeanClassName, Class defaultActionBeanClassForUriSyntaxError, Class defaultActionBeanClassForNotFounds)
	{
		if (actionBeanClassName == null)
			return defaultActionBeanClassForNotFounds;
		
		try
		{
			return Class.forName(actionBeanClassName);
		}
		catch (ClassNotFoundException exc)  //normally happens on 404's!
		{
			return defaultActionBeanClassForNotFounds;
		}
	}
	
	
	
	
	
	
	/**
	 * Instantiate an {@link ActionBean} and invoke {@link ActionBean#setActionBeanContext(ActionBeanContext)} on it with a newly created {@link ActionBeanContext}.<br>
	 * @return The action bean all initted and its context set, or <code>null</code> if there was an error instantiating it (which has been {@link ServletContext#log(String, Throwable) logged}.
	 */
	public static @Nullable <T extends ActionBean> T newActionBean(Class<T> clazz, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
	{
		if (clazz == null)
		{
			servletContext.log("No actionbean for "+request.getRequestURI());
			return null;
		}
		
		try
		{
			T bean = clazz.newInstance();
			
			if (bean instanceof ActionBean)
			{
				bean.setActionBeanContext(new SimpleImmutableActionBeanContext(request, response, servletContext));
				return bean;
			}
			else
			{
				servletContext.log("Requested action bean "+clazz+" is not an ActionBean!");
				return null;
			}
		}
		catch (InstantiationException exc)
		{
			servletContext.log("Error instantiating action bean "+clazz+":", exc);
			return null;
		}
		catch (IllegalAccessException exc)
		{
			servletContext.log("Error instantiating action bean " + clazz + ":", exc);
			return null;
		}
	}
}
