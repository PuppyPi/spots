package rebound.spots.util;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class FlushPendingServletResponseOutputStream
extends ServletOutputStream
{
	protected final ServletOutputStream underlying;
	
	public FlushPendingServletResponseOutputStream(ServletOutputStream underlying)
	{
		this.underlying = underlying;
	}
	
	
	
	@Override
	public void flush() throws IOException
	{
		//underlying.flush();
		
		//NOPE! XD
		//use reallyFlush() on our parent FlushPendingServletResponseDecorator :>
	}
	
	@Override
	public void close() throws IOException
	{
		//underlying.close();
		
		//NOPE! XD
		//use reallyClose() on our parent FlushPendingServletResponseDecorator :>
	}
	
	
	
	@Override
	public int hashCode()
	{
		return this.underlying.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.underlying.equals(obj);
	}
	
	@Override
	public void print(boolean arg0) throws IOException
	{
		this.underlying.print(arg0);
	}
	
	@Override
	public void print(char c) throws IOException
	{
		this.underlying.print(c);
	}
	
	@Override
	public void print(double d) throws IOException
	{
		this.underlying.print(d);
	}
	
	@Override
	public void print(float f) throws IOException
	{
		this.underlying.print(f);
	}
	
	@Override
	public void print(int i) throws IOException
	{
		this.underlying.print(i);
	}
	
	@Override
	public void print(long l) throws IOException
	{
		this.underlying.print(l);
	}
	
	@Override
	public void print(String arg0) throws IOException
	{
		this.underlying.print(arg0);
	}
	
	@Override
	public void println() throws IOException
	{
		this.underlying.println();
	}
	
	@Override
	public void println(boolean b) throws IOException
	{
		this.underlying.println(b);
	}
	
	@Override
	public void println(char c) throws IOException
	{
		this.underlying.println(c);
	}
	
	@Override
	public void println(double d) throws IOException
	{
		this.underlying.println(d);
	}
	
	@Override
	public void println(float f) throws IOException
	{
		this.underlying.println(f);
	}
	
	@Override
	public void println(int i) throws IOException
	{
		this.underlying.println(i);
	}
	
	@Override
	public void println(long l) throws IOException
	{
		this.underlying.println(l);
	}
	
	@Override
	public void println(String s) throws IOException
	{
		this.underlying.println(s);
	}
	
	@Override
	public void write(int b) throws IOException
	{
		this.underlying.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		this.underlying.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		this.underlying.write(b, off, len);
	}
	
	@Override
	public String toString()
	{
		return this.underlying.toString();
	}
	
	
	
	
	
	
	//// New for Servlet API > 2.5 ! ////
	@Override
	public boolean isReady()
	{
		return this.underlying.isReady();
	}
	
	@Override
	public void setWriteListener(WriteListener writeListener)
	{
		this.underlying.setWriteListener(writeListener);
	}
}
