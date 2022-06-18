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
import rebound.simplejee.ReplacementErrorResolutionResponseWrapper;

public abstract class ErrorResolutionReplacementFilter
extends AbstractHTTPFilter
{
	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		chain.doFilter(request, new ReplacementErrorResolutionResponseWrapper(request, response, getServletContext(), this::sendError));
	}
	
	public abstract boolean sendError(HttpServletRequest request, HttpServletResponse response, ServletContext context, int code, String message) throws IOException;
}
