package rebound.simplejee;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.annotations.semantic.temporal.concurrencyprimitives.threadspecification.AnyThreads;

public abstract class AbstractHttpServlet
extends AbstractServlet
{
	@AnyThreads
	public abstract void serviceHttp(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	
	
	@AnyThreads
	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
	{
		HttpServletRequest request;
		HttpServletResponse response;
		
		if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse))
			throw new ServletException("non-HTTP request or response");
		
		request = (HttpServletRequest) req;
		response = (HttpServletResponse) res;
		
		serviceHttp(request, response);
	}
}
