package rebound.simplejee;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ReplacementErrorResolutionResponseWrapper
extends HttpServletResponseWrapper
{
	protected final SimpleJEEErrorHandler handler;
	
	public ReplacementErrorResolutionResponseWrapper(HttpServletResponse response, SimpleJEEErrorHandler handler)
	{
		super(response);
		this.handler = handler;
	}
	
	
	
	@Override
	public void sendError(int code) throws IOException
	{
		if (!handler.sendError(code, null))
			super.sendError(code);
	}
	
	@Override
	public void sendError(int code, String msg) throws IOException
	{
		if (!handler.sendError(code, msg))
			super.sendError(code, msg);
	}
}
