package rebound.simplejee;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.spots.util.ErrorResolutionReplacementFilter;

public interface SimpleJEEErrorHandler
{
	/**
	 * This handles the HTTP Coded errors (404, 400, 401, 403, 503, etc.).<br>
	 * Regardless of what headers and body the implementation uses, it should invoke HttpServletResponse.setStatus() with at least the code.<br>
	 * This method effectively takes the place of {@link HttpServletResponse#sendError(int, String)} (and the other with no string message). A delegating method is inserted by {@link ErrorResolutionReplacementFilter}.<br>
	 * @param context The context (HttpServletRequest, HttpServletResponse, ServletConfig)
	 * @param code The status code (404,500,…)
	 * @param message An optional message to send to the client, may be <code>null</code>
	 * @return <code>true</code> if the error was handled, <code>false</code> if the underlying response should handle the error (if <code>false</code> is returned, the response must not be committed).
	 */
	public boolean sendError(HttpServletRequest request, HttpServletResponse response, ServletContext context, int code, String message) throws IOException;
}