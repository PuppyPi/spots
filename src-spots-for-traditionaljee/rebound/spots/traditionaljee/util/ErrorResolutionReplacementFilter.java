/*
 * Created on Feb 28, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.traditionaljee.util;

import java.io.IOException;
import javax.servlet.FilterChain;
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
		chain.doFilter(request, new ReplacementErrorResolutionResponseWrapper(response, (int code, String message) -> this.sendError(request, response, code, message)));
	}
	
	public abstract boolean sendError(HttpServletRequest request, HttpServletResponse response, int code, String message) throws IOException;
}
