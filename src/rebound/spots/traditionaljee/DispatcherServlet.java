/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.traditionaljee;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.spots.ActionBean;
import rebound.spots.ActionBeanContext;


/**
 * This servlet doles out requests to {@link ActionBean}s.<br>
 * It does not use the URI of the request to determine which ActionBean to instantiate.<br>
 * Instead, it relies on a Filter to have set the request attribute {@link DispatcherServlet#RESOLVED_ATTRIBUTE} to the Class of the ActionBean.<br>
 * If that attribute is <code>null</code>, 500 is sent in the response.<br>
 * <br>
 * <br>
 * <br>
 * Configuration (all keys are case-sensitive, according to the container of course):
 * <table>
 * 	<tr>
 * 		<td>"debug"</td>
 * 		<td>"true" or "false"</td>
 * 		<td>
 * 			Debug mode (more output logged).<br>
 * 		</td>
 * 	</tr>
 * 	
 * 	<tr>
 * 		<td>"<i>actionbean</i>.<i>parameter</i>"</td>
 * 		<td>
 * 			<i>actionbean</i> - Fully qualified or simple name of an action bean class<br>
 * 			<i>parameter</i> - ActionBean-specific parameter's name.
 * 		</td>
 * 		<td>
 * 			An ActionBean-specific parameter.<br>
 * 		</td>
 * 	</tr>
 * </table>
 * @author RProgrammer
 */
public class DispatcherServlet
extends HttpServlet
{
	public static final String RESOLVED_ATTRIBUTE = "actionBean.class";
	public static final String EXCEPTION_HANDLER_ATTRIBUTE = "exceptionHandler.class";
	
	
	protected String contextVar;
	public String getActionBeanContextAttribute()
	{
		if (contextVar == null)
		{
			contextVar = getServletConfig().getInitParameter("ActionBeanContextAttribute");
			if (contextVar == null)
				contextVar = "actioncontext";
		}
		return contextVar;
	}
	
	
	protected boolean debug, debugSet;
	public boolean isDebug()
	{
		if (!debugSet)
		{
			debug = "true".equalsIgnoreCase(getServletConfig().getInitParameter("debug"));
			debugSet = true;
		}
		return debug;
	}
	
	
	
	
	
	
	
	
	
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		//Try to instantiate and run an actionbean
		//Don't pay any attention to the METHOD, the beans take care of that.
		
		//<Pull helper objects from attributes
		Class actionBeanClass = null;
		{
			Object o = request.getAttribute(RESOLVED_ATTRIBUTE);
			if (o instanceof Class)
				actionBeanClass = (Class)o;
		}
		//Pull helper objects from attributes>
		
		
		//<Load bean
		ActionBeanContext context = getActionBeanContext(actionBeanClass, request, response);
		ActionBean bean = getActionBean(actionBeanClass, request, response, context);
		//Load bean>
		
		
		//<Use bean
		if (bean == null)
		{
			log("Error instantiating "+(actionBeanClass == null ? null : actionBeanClass.getName())+" for "+request.getRequestURI(), false);
			String msg = "Error getting actionbean "+(actionBeanClass == null ? null : actionBeanClass.getName());
			response.sendError(500, msg);
		}
		else
		{
			log("Invoking "+actionBeanClass.getName()+" for "+request.getRequestURI());
			bean.doAction();
		}
		//Use bean>
	}
	
	
	
	
	
	
	//<Beany
	/**
	 * Instantiate an {@link ActionBean} and invoke {@link ActionBean#setContext(ActionBeanContext)} on it with a newly created {@link ActionBeanContext}.<br>
	 * @return The action bean all initted and its context set, or <code>null</code> if there was an error instantiating it.
	 */
	public ActionBean getActionBean(Class clazz, HttpServletRequest request, HttpServletResponse response, ActionBeanContext context)
	{
		if (clazz == null)
		{
			log("No actionbean for "+request.getRequestURI(), false);
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
					
					bean.setContext(context);
				}
				else
				{
					log
					(
						"Requested action bean "+clazz+" is not an ActionBean!",
						false
					);
				}
			}
			
			return bean;
		}
		
		catch (InstantiationException exc)
		{
			log
			(
				"Error instantiating action bean "+clazz+":",
				exc
			);
		}
		catch (IllegalAccessException exc)
		{
			log
			(
				"Error instantiating action bean "+clazz+":",
				exc
			);
		}
		
		return null;
	}
	
	
	public ActionBeanContext getActionBeanContext(Class<? extends ActionBean> actionBeanClass, HttpServletRequest request, HttpServletResponse response)
	{
		return new MyActionBeanContext(actionBeanClass, request, response);
	}
	
	
	
	protected class MyActionBeanContext
	implements ExtraActionBeanContext
	{
		protected HttpServletRequest request;
		protected HttpServletResponse response;
		protected Class beanClass;
		
		public MyActionBeanContext()
		{
			super();
		}
		
		public MyActionBeanContext(Class beanClass, HttpServletRequest request, HttpServletResponse response)
		{
			super();
			setRequest(request);
			setResponse(response);
			setBeanClass(beanClass);
		}
		
		
		
		public String getAParameter(String name)
		{
			String param = null;
			
//			if (param == null)
			param = getInitParameter(name);
			
			if (param == null)
				param = getGlobalInitParameter(name);
			
			return param;
		}
		
		
		public String getInitParameter(String name)
		{
			return DispatcherServlet.this.getActionBeanInitParameter(getBeanClass(), name);
		}
		
		public String[] getInitParameterNames()
		{
			return DispatcherServlet.this.getActionBeanInitParameters(getBeanClass());
		}
		
		public String getGlobalInitParameter(String name)
		{
			return DispatcherServlet.this.getInitParameter(name);
		}
		
		public String[] getGlobalInitParameterNames()
		{
			return DispatcherServlet.this.getInitParameters();
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
			return DispatcherServlet.this.getServletContext();
		}
		
		public Class getBeanClass()
		{
			return this.beanClass;
		}
		
		public void setBeanClass(Class beanClass)
		{
			this.beanClass = beanClass;
		}
		
		public void setRequest(HttpServletRequest request)
		{
			this.request = request;
			request.setAttribute(getActionBeanContextAttribute(), this);
		}
		
		public void setResponse(HttpServletResponse response)
		{
			this.response = response;
		}
	}
	//Beany>
	
	
	
	
	//<Params
	protected String[] paramNames;
	public synchronized String[] getInitParameters()
	{
		if (paramNames == null)
		{
			Vector<String> list = new Vector<String>();
			
			Enumeration<String> e = getServletConfig().getInitParameterNames();
			
			while (e.hasMoreElements())
				list.add(e.nextElement());
			
			paramNames = list.toArray(new String[list.size()]);
		}
		return paramNames;
	}
	
	
	
	protected Hashtable<Class, String[]> beanParamsCache = new Hashtable<Class, String[]>();
	public String[] getActionBeanInitParameters(Class actionBeanClass)
	{
		String[] params = null;
		{
			synchronized (beanParamsCache)
			{
				params = beanParamsCache.get(actionBeanClass);
				if (params == null)
				{
					//Calculate them and store that (hence the "cache" part)
					
					String prefix1 = actionBeanClass.getName()+'.';
					String prefix2 = actionBeanClass.getSimpleName()+'.';
					
					String[] globals = getInitParameters();
					String[] buff = new String[globals.length];
					int buffIndex = 0;
					boolean hasPrefix1 = false, hasPrefix2 = false;
					for (String global : globals)
					{
						hasPrefix1 = global.startsWith(prefix1);
						if (!hasPrefix1)
							hasPrefix2 = global.startsWith(prefix2);
						
						if (hasPrefix1 || hasPrefix2)
						{
							String relativeName = null;
							{
								if (hasPrefix1)
									relativeName = global.substring(prefix1.length());
								else// if (hasPrefix2)
									relativeName = global.substring(prefix2.length());
							}
							
							buff[buffIndex] = relativeName;
							buffIndex++;
						}
					}
					
					params = new String[buffIndex];
					System.arraycopy(buff, 0, params, 0, buffIndex);
				}
			}
		}
		
		return params;
	}
	
	public String getActionBeanInitParameter(Class actionBeanClass, String name)
	{
		String param = null;
		
//		if (param == null)
		param = getInitParameter(actionBeanClass.getName()+'.'+name);
		
		if (param == null)
			param = getInitParameter(actionBeanClass.getSimpleName()+'.'+name);
		
		return param;
	}
	//Params>
	
	
	
	
	
	
	public void log(String msg)
	{
		log(msg, true); //Show rarely
	}
	
	public void log(String msg, boolean quiet)
	{
		if (!quiet || isDebug())
		{
			//super.log(msg);
			
			@SuppressWarnings("resource")   //Eclipse, my friend, buddy..we don't want to close things we didn't open!  ESPECIALLY STDOUT/ERR X'DDD
			PrintStream out = quiet ? System.out : System.err;
			
			out.println("DispatcherServlet "+new Date()+") "+msg);
		}
	}
	
	public void log(String msg, Throwable exc)
	{
		log(msg, exc, false); //Show always
	}
	
	public void log(String msg, Throwable exc, boolean quiet)
	{
		if (!quiet || isDebug())
		{
			//super.log(msg, exc);
			
			PrintStream out = quiet ? System.out : System.err;
			out.println("DispatcherServlet "+new Date()+") "+msg);
			out.println(exc.getClass().getName()+": "+exc.getMessage());
			exc.printStackTrace(out);
		}
	}
}
