package rebound.simplejee;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Send the final flush and close with {@link #reallyClose()}!
 * And do an actual flush with {@link #reallyFlush()}
 */
public class FlushPendingHttpServletResponseDecorator
implements HttpServletResponse
{
	protected HttpServletResponse underlying;
	protected ServletOutputStream underlyingOut;
	protected PrintWriter underlyingWOut;
	protected boolean pendingClose = false;
	
	public FlushPendingHttpServletResponseDecorator(HttpServletResponse underlying)
	{
		this.underlying = underlying;
	}
	
	
	@Override
	public void flushBuffer() throws IOException
	{
		//underlying.flushBuffer();
		
		//NOPE! XD
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		if (this.underlyingWOut != null)
			throw new IllegalStateException("Servlets can't use both text and octet output streams!");
		
		if (this.underlyingOut == null)
			this.underlyingOut = new FlushPendingServletResponseOutputStream(this.underlying.getOutputStream());
		return this.underlyingOut;
	}
	
	@Override
	public PrintWriter getWriter() throws IOException
	{
		if (this.underlyingOut != null)
			throw new IllegalStateException("Servlets can't use both text and octet output streams!");
		
		if (this.underlyingWOut == null)
			this.underlyingWOut = new FlushPendingServletResponsePrintWriter(this.underlying.getWriter());
		return this.underlyingWOut;
	}
	
	
	public void reallyClose() throws IOException
	{
		if (underlyingWOut != null)
		{
			underlying.getWriter().close();
		}
		
		if (underlyingOut != null)
		{
			underlying.getOutputStream().close();
		}
	}
	
	
	public void reallyFlush() throws IOException
	{
		this.underlying.getWriter().flush();  //it might have its own text-encoding buffer, idk!
		this.underlying.getOutputStream().flush();
		this.underlying.flushBuffer();
	}
	
	
	
	
	
	@Override
	public void addCookie(Cookie arg0)
	{
		this.underlying.addCookie(arg0);
	}
	
	@Override
	public void addDateHeader(String arg0, long arg1)
	{
		this.underlying.addDateHeader(arg0, arg1);
	}
	
	@Override
	public void addHeader(String arg0, String arg1)
	{
		this.underlying.addHeader(arg0, arg1);
	}
	
	@Override
	public void addIntHeader(String arg0, int arg1)
	{
		this.underlying.addIntHeader(arg0, arg1);
	}
	
	@Override
	public boolean containsHeader(String arg0)
	{
		return this.underlying.containsHeader(arg0);
	}
	
	@Override
	public String encodeRedirectURL(String arg0)
	{
		return this.underlying.encodeRedirectURL(arg0);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public String encodeRedirectUrl(String arg0)
	{
		return this.underlying.encodeRedirectUrl(arg0);
	}
	
	@Override
	public String encodeURL(String arg0)
	{
		return this.underlying.encodeURL(arg0);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public String encodeUrl(String arg0)
	{
		return this.underlying.encodeUrl(arg0);
	}
	
	@Override
	public int getBufferSize()
	{
		return this.underlying.getBufferSize();
	}
	
	@Override
	public String getCharacterEncoding()
	{
		return this.underlying.getCharacterEncoding();
	}
	
	@Override
	public String getContentType()
	{
		return this.underlying.getContentType();
	}
	
	@Override
	public Locale getLocale()
	{
		return this.underlying.getLocale();
	}
	
	@Override
	public boolean isCommitted()
	{
		return this.underlying.isCommitted();
	}
	
	@Override
	public void reset()
	{
		this.underlying.reset();
	}
	
	@Override
	public void resetBuffer()
	{
		this.underlying.resetBuffer();
	}
	
	@Override
	public void sendError(int arg0, String arg1) throws IOException
	{
		this.underlying.sendError(arg0, arg1);
	}
	
	@Override
	public void sendError(int arg0) throws IOException
	{
		this.underlying.sendError(arg0);
	}
	
	@Override
	public void sendRedirect(String arg0) throws IOException
	{
		this.underlying.sendRedirect(arg0);
	}
	
	@Override
	public void setBufferSize(int arg0)
	{
		this.underlying.setBufferSize(arg0);
	}
	
	@Override
	public void setCharacterEncoding(String arg0)
	{
		this.underlying.setCharacterEncoding(arg0);
	}
	
	@Override
	public void setContentLength(int arg0)
	{
		this.underlying.setContentLength(arg0);
	}
	
	@Override
	public void setContentType(String arg0)
	{
		this.underlying.setContentType(arg0);
	}
	
	@Override
	public void setDateHeader(String arg0, long arg1)
	{
		this.underlying.setDateHeader(arg0, arg1);
	}
	
	@Override
	public void setHeader(String arg0, String arg1)
	{
		this.underlying.setHeader(arg0, arg1);
	}
	
	@Override
	public void setIntHeader(String arg0, int arg1)
	{
		this.underlying.setIntHeader(arg0, arg1);
	}
	
	@Override
	public void setLocale(Locale arg0)
	{
		this.underlying.setLocale(arg0);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void setStatus(int arg0, String arg1)
	{
		this.underlying.setStatus(arg0, arg1);
	}
	
	@Override
	public void setStatus(int arg0)
	{
		this.underlying.setStatus(arg0);
	}
	
	
	
	//// New for Servlet API > 2.5 ! ////
	@Override
	public void setContentLengthLong(long len)
	{
		this.underlying.setContentLengthLong(len);
	}
	
	@Override
	public int getStatus()
	{
		return this.underlying.getStatus();
	}
	
	@Override
	public String getHeader(String name)
	{
		return this.underlying.getHeader(name);
	}
	
	@Override
	public Collection<String> getHeaders(String name)
	{
		return this.underlying.getHeaders(name);
	}
	
	@Override
	public Collection<String> getHeaderNames()
	{
		return this.underlying.getHeaderNames();
	}
	
	public void setTrailerFields(Supplier<Map<String, String>> supplier)
	{
		this.underlying.setTrailerFields(supplier);
	}
	
	public Supplier<Map<String, String>> getTrailerFields()
	{
		return this.underlying.getTrailerFields();
	}
}
