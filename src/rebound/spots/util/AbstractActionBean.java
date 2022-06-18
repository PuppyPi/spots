/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import rebound.annotations.hints.IntendedToBeSubclassedImplementedOrOverriddenByApiUser;
import rebound.spots.ActionBean;
import rebound.spots.ActionBeanContext;

public abstract class AbstractActionBean
implements ActionBean
{
	protected ActionBeanContext context;
	
	
	public void setContext(ActionBeanContext context)
	{
		this.context = context;
	}
	
	public ActionBeanContext getContext()
	{
		return this.context;
	}
	
	
	
	
	//<Utils
	/**
	 * You may override this to change it, but by default, it pulls from the configuration.<br>
	 * It controls the level of output.<br>
	 */
	@IntendedToBeSubclassedImplementedOrOverriddenByApiUser
	public boolean isDebug()
	{
		return false;
	}
	
	@IntendedToBeSubclassedImplementedOrOverriddenByApiUser
	public boolean isBenchmarking()
	{
		return isDebug(); 
	}
	
	public void log(String msg)
	{
		log(msg, false);
	}
	
	public void log(String msg, boolean debug)
	{
		if (!debug || isDebug())
			getContext().getServletContext().log(getClass().getSimpleName()+": "+msg);
	}
	
	public void logBenchmark(long start, String desc)
	{
		long end = System.currentTimeMillis();
		
		if (isBenchmarking())
			log("Benchmark: "+desc+" took "+(end - start)+"ms");
	}
	
	public void log(String msg, Throwable t)
	{
		getContext().getServletContext().log(getClass().getSimpleName()+": "+msg, t);
	}
	
	
	public HttpServletRequest getRequest()
	{
		return getContext().getRequest();
	}
	
	public HttpSession getSession()
	{
		return getContext().getRequest().getSession();
	}
	
	public HttpServletResponse getResponse()
	{
		return getContext().getResponse();
	}
	//Utils>
}
