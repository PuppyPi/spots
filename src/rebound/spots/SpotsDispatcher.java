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

public class SpotsDispatcher
{
	/**
	 * You might want to not just pass {@link #getActionBeanClass(String, Class)} or similar into here in your {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)} implementation, but consider making your own getActionBeanClass(String) (especially if your URLs can have database-based non-static parts!) so that other things in the system can check URLs (really URI path parts) to see if they're servable by your webapp!  (eg, when selecting a name for the dynamic user-generated part to tell if it overlaps with a page! like if they try making their username be "about" or "robots.txt" XD )
	 * Or better, usually, write a method <code>public static {@link PairOrdered}<Class, String> getActionBeanClassAndViewResourcePathname(String requestURIPath)</code> or similar, which also provides the default resource pathname that *would* be used (whether it is or not) by the Action Bean for {@link SimpleJEEUtilities#serveStatically(ServletContext, HttpServletRequest, HttpServletResponse, String)} or {@link SimpleJEEUtilities#serveJSP(ServletContext, HttpServletRequest, HttpServletResponse, String)} :>
	 * 
	 * @param servletContext  usually from {@link Servlet#getServletConfig()}.{@link ServletConfig#getServletContext()}
	 * @param request  usually from {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)}
	 * @param response  usually from {@link AbstractHttpServlet#serviceHttp(HttpServletRequest, HttpServletResponse)}
	 * @param actionBeanClass  usually from {@link #getActionBeanClass(String, Class)} or similar (which, itself, pulls from {@link #getActionBeanClassName(String, String, String)} or similar)
	 * @param verbose  log even non error hits to {@link ServletContext#log(String)} ?
	 */
	public static void dispatch(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, Class actionBeanClass, boolean verbose) throws ServletException, IOException
	{
		//Try to instantiate and run an actionbean
		//Don't pay any attention to the HTTP METHOD, the beans take care of that.
		
		
		ActionBean bean = newActionBean(actionBeanClass, servletContext, request, response);
		
		
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
			
			bean.doAction();
		}
		//Use bean>
	}
	
	
	
	
	
	
	/**
	 * The Java Fully-Qualified Class Name of the action bean:
	 * 
	 * eg, (if prefix = "com.example.webui.actions." and suffix = "Action"),
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
	 * @param actionBeanPrefix  eg, "com.example.webui.actions."
	 * @param actionBeanSuffix  eg, "Action"
	 * @return null on syntax error (eg, doesn't start with "/")
	 */
	public static @Nullable String getActionBeanClassName(@Nonnull String requestURIPath, String actionBeanPrefix, String actionBeanSuffix)
	{
		//We use Unmodified by default to be consistent with most of the web, wherein URL/URI paths are totally case-sensitive
		return getActionBeanClassName(requestURIPath, actionBeanPrefix, actionBeanSuffix, ClassNameCapitalization.Unmodified, '_', '_', '_');
	}
	
	
	public static enum ClassNameCapitalization
	{
		Unmodified,  //introduces no non-injectivities
		UppercaseFirstLetterOfSimpleNamePart,
		LowercaseAllAndUppercaseFirstLetterOfSimpleNamePart,
	}
	
	public static @Nullable String getActionBeanClassName(@Nonnull String requestURIPath, String actionBeanPrefix, String actionBeanSuffix, ClassNameCapitalization capitalizationPreference, char charForDots, char charForDashes, char charForSpaces)
	{
		if (requestURIPath.isEmpty())
			return null;
		
		if (requestURIPath.charAt(0) != '/')
			return null;
		
		//Trim trailing slash
		requestURIPath = rtrimstr(requestURIPath, "/");
		
		//The stem section is between the prefix and the suffix
		String actionBeanStem;
		{
			//It has the uri-dots (.css, .html, ...) replaced with internal-class dollar signs and its uri-slashes replaced with package-dots
			String[] pathElements = split(requestURIPath, '/');
			
			int n = pathElements.length;
			
			for (int i = 0; i < n; i++)
			{
				String pathElement = pathElements[i];
				pathElement = urldescape(pathElement);
				pathElement = pathElement.replace('.', charForDots).replace('-', charForDashes).replace(' ', charForSpaces);  //Some standard-to-Spots conversions for characters legal in URLs, but illegal in Java identifiers
				
				//NOTE that dots are converted *after* url descaping!!
				//This way, if %2E appears, it won't allow an attacker to get to a (sub)package you might not expect them to!
			}
			actionBeanStem = joinStrings('.', pathElements);  //Convert to a package
		}
		
		
		//Attach the prefix+stem+suffix
		String actionBeanClassName = actionBeanPrefix + actionBeanStem + actionBeanSuffix;
		
		
		//Capitalize the classname (regardless of whether it's in the actionBeanStem or actionBeanSuffix!
		{
			int lastDot = actionBeanClassName.lastIndexOf('.');
			int capsCharIndex = 0;
			
			if (lastDot != -1)
			{
				capsCharIndex = lastDot + 1;
				if (capsCharIndex >= actionBeanClassName.length())
					capsCharIndex = lastDot; //Don't blame me for there not being a classname!
				
				actionBeanClassName =
				actionBeanClassName.substring(0, capsCharIndex) +
				Character.toUpperCase(actionBeanClassName.charAt(capsCharIndex)) +
				actionBeanClassName.substring(capsCharIndex+1);
			}
		}
		
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
	 * Instantiate an {@link ActionBean} and invoke {@link ActionBean#setContext(ActionBeanContext)} on it with a newly created {@link ActionBeanContext}.<br>
	 * @return The action bean all initted and its context set, or <code>null</code> if there was an error instantiating it (which has been {@link ServletContext#log(String, Throwable) logged}.
	 */
	public static @Nullable ActionBean newActionBean(Class clazz, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
	{
		if (clazz == null)
		{
			servletContext.log("No actionbean for "+request.getRequestURI());
			return null;
		}
		
		try
		{
			ActionBean bean = null;
			{
				Object o = clazz.newInstance();
				
				if (o instanceof ActionBean)
				{
					bean = (ActionBean)o;
					
					bean.setContext(new SimpleImmutableActionBeanContext(request, response, servletContext));
				}
				else
				{
					servletContext.log("Requested action bean "+clazz+" is not an ActionBean!");
				}
			}
			
			return bean;
		}
		
		catch (InstantiationException exc)
		{
			servletContext.log("Error instantiating action bean "+clazz+":", exc);
		}
		catch (IllegalAccessException exc)
		{
			servletContext.log("Error instantiating action bean " + clazz + ":", exc);
		}
		
		return null;
	}
}
