package rebound.spots.util;

import java.io.PrintWriter;
import java.util.Locale;

public class FlushPendingServletResponsePrintWriter
extends PrintWriter
{
	protected final PrintWriter underlying;
	
	public FlushPendingServletResponsePrintWriter(PrintWriter underlying)
	{
		super(underlying);
		this.underlying = underlying;
	}
	
	
	@Override
	public void flush()
	{
		//underlying.flush();
		
		//NOPE! XD
		//use reallyFlush() on our parent FlushPendingServletResponseDecorator :>
	}
	
	@Override
	public void close()
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
	public String toString()
	{
		return this.underlying.toString();
	}
	
	@Override
	public boolean checkError()
	{
		return this.underlying.checkError();
	}
	
	@Override
	public void write(int c)
	{
		this.underlying.write(c);
	}
	
	@Override
	public void write(char[] buf, int off, int len)
	{
		this.underlying.write(buf, off, len);
	}
	
	@Override
	public void write(char[] buf)
	{
		this.underlying.write(buf);
	}
	
	@Override
	public void write(String s, int off, int len)
	{
		this.underlying.write(s, off, len);
	}
	
	@Override
	public void write(String s)
	{
		this.underlying.write(s);
	}
	
	@Override
	public void print(boolean b)
	{
		this.underlying.print(b);
	}
	
	@Override
	public void print(char c)
	{
		this.underlying.print(c);
	}
	
	@Override
	public void print(int i)
	{
		this.underlying.print(i);
	}
	
	@Override
	public void print(long l)
	{
		this.underlying.print(l);
	}
	
	@Override
	public void print(float f)
	{
		this.underlying.print(f);
	}
	
	@Override
	public void print(double d)
	{
		this.underlying.print(d);
	}
	
	@Override
	public void print(char[] s)
	{
		this.underlying.print(s);
	}
	
	@Override
	public void print(String s)
	{
		this.underlying.print(s);
	}
	
	@Override
	public void print(Object obj)
	{
		this.underlying.print(obj);
	}
	
	@Override
	public void println()
	{
		this.underlying.println();
	}
	
	@Override
	public void println(boolean x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(char x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(int x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(long x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(float x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(double x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(char[] x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(String x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public void println(Object x)
	{
		this.underlying.println(x);
	}
	
	@Override
	public PrintWriter printf(String format, Object... args)
	{
		return this.underlying.printf(format, args);
	}
	
	@Override
	public PrintWriter printf(Locale l, String format, Object... args)
	{
		return this.underlying.printf(l, format, args);
	}
	
	@Override
	public PrintWriter format(String format, Object... args)
	{
		return this.underlying.format(format, args);
	}
	
	@Override
	public PrintWriter format(Locale l, String format, Object... args)
	{
		return this.underlying.format(l, format, args);
	}
	
	@Override
	public PrintWriter append(CharSequence csq)
	{
		return this.underlying.append(csq);
	}
	
	@Override
	public PrintWriter append(CharSequence csq, int start, int end)
	{
		return this.underlying.append(csq, start, end);
	}
	
	@Override
	public PrintWriter append(char c)
	{
		return this.underlying.append(c);
	}
}
