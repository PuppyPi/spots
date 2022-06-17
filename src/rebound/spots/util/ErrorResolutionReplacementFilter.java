/*
 * Created on Feb 28, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.util;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public abstract class ErrorResolutionReplacementFilter
extends AbstractHTTPFilter
{
	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		chain.doFilter(request, new ReplacementErrorResolutionResponseWrapper(request, response));
	}
	
	
	
	
	
	/**
	 * This handles the HTTP Coded errors (404, 400, 401, 403, 503, etc.).<br>
	 * Regardless of what headers and body the implementation uses, it should invoke HttpServletResponse.setStatus() with at least the code.<br>
	 * This method effectively takes the place of {@link HttpServletResponse#sendError(int, String)} (and the other with no string message). A delegating method is inserted by {@link ErrorResolutionReplacementFilter}.<br>
	 * @param context The context (HttpServletRequest, HttpServletResponse, ServletConfig)
	 * @param code The status code (404,500,…)
	 * @param message An optional message to send to the client, may be <code>null</code>
	 * @return <code>true</code> if the error was handled, <code>false</code> if the underlying response should handle the error (if <code>false</code> is returned, the response must not be committed).
	 */
	public abstract boolean sendError(HttpServletRequest request, HttpServletResponse response, ServletContext context, int code, String message) throws IOException;
	
	
	
	
	
	
	
	
	
	
	protected class ReplacementErrorResolutionResponseWrapper
	extends HttpServletResponseWrapper
	{
		protected HttpServletRequest request;
		
		public ReplacementErrorResolutionResponseWrapper(HttpServletRequest request, HttpServletResponse response)
		{
			super(response);
			setRequest(request);
		}
		
		
		
		//<Actual Overrides
		@Override
		public void sendError(int code) throws IOException
		{
			if (ErrorResolutionReplacementFilter.this.sendError(getRequest(), this, getServletContext(), code, null))
				return;
			
			super.sendError(code);
		}
		
		@Override
		public void sendError(int code, String msg) throws IOException
		{
			if (ErrorResolutionReplacementFilter.this.sendError(getRequest(), this, getServletContext(), code, msg))
				return;
			
			super.sendError(code, msg);
		}
		//Actual Overrides>
		
		
		
		
		
		
		public HttpServletRequest getRequest()
		{
			return this.request;
		}
		
		public void setRequest(HttpServletRequest request)
		{
			this.request = request;
		}
	}
}
