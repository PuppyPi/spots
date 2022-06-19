/*
 * Created on Jun 2, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util.binding.annotated;

import rebound.spots.util.binding.annotated.typeconversion.ConverterSpecifier;

/**
 * You must have bound a field without a {@link ConverterSpecifier specifier} and used a type which is not supported (see {@link ConverterSpecifier}). For shame!<br>
 * 
 * ——TODO This is never used!! X'DD
 * 
 * @author RProgrammer
 */
public class TypeNotSupportedException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	protected Class unsupported;
	
	public TypeNotSupportedException()
	{
		super();
	}
	
	public TypeNotSupportedException(Class unsupported)
	{
		super();
		this.unsupported = unsupported;
	}
	
	
	public Class getUnsupported()
	{
		return this.unsupported;
	}
}
