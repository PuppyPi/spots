package rebound.simplejee;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import rebound.annotations.semantic.temporal.concurrencyprimitives.threadspecification.AnyThreads;

/**
 * Remember, the HTTP Sessions ({@link HttpSession}s) are available from {@link HttpServletRequest#getSession(boolean)} and, in Simple JEE, web.xml doesn't set the
 * session timeout.  You set the session timeout! With {@link HttpSession#setMaxInactiveInterval(int)} (and can do so differently for different URIs and/or do so by
 * pulling from an SQL database of config settings, etc.! :D )
 */
public abstract class AbstractHttpServlet
extends AbstractServlet
{
	@AnyThreads
	public abstract void serviceHttp(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	
	
	@AnyThreads
	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException
	{
		if (!(request instanceof HttpServletRequest))
			throw new ServletException("a ServletRequest that wasn't an HttpServletRequest was given to us, an HTTP-expecting Servlet!");
		
		if (!(response instanceof HttpServletResponse))
			throw new ServletException("a ServletResponse that wasn't an HttpServletResponse was given to us, an HTTP-expecting Servlet!");
		
		serviceHttp((HttpServletRequest) request, (HttpServletResponse) response);
	}
}
