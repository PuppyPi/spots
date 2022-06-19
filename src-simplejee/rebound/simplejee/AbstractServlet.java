package rebound.simplejee;

import static java.util.Objects.*;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import rebound.annotations.hints.IntendedToOptionallyBeSubclassedImplementedOrOverriddenByApiUser;
import rebound.annotations.semantic.temporal.concurrencyprimitives.threadspecification.AnyThreads;

/**
 * You must also have a public, no-args constructor to count as a servlet!
 */
public abstract class AbstractServlet
implements Servlet
{
	protected ServletConfig config;
	
	@Override
	@IntendedToOptionallyBeSubclassedImplementedOrOverriddenByApiUser
	public void init(ServletConfig config) throws ServletException
	{
		requireNonNull(config);
		this.config = config;
	}
	
	@Override
	@IntendedToOptionallyBeSubclassedImplementedOrOverriddenByApiUser
	public void destroy()
	{
	}
	
	
	
	
	
	@AnyThreads
	@Override
	public abstract void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;
	
	
	
	
	
	@Override
	public ServletConfig getServletConfig()
	{
		return config;
	}
	
	@Override
	public String getServletInfo()
	{
		return "";
	}
}
