/*
 * Created on Sep 30, 2007
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.annotated.typeconversion;

import java.lang.annotation.Annotation;

public class TypeConversionException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	protected TypeConverter converter;
	protected Class destination;
	protected Annotation specifier;
	protected Annotation[] environment;
	
	public TypeConversionException()
	{
		super();
	}
	
	public TypeConversionException(TypeConverter converter, Class destination, Annotation specifier, Annotation[] environment, Exception cause)
	{
		super(cause);
		this.converter = converter;
		this.destination = destination;
		this.specifier = specifier;
		this.environment = environment;
	}
	
	
	public TypeConverter getTypeConverter()
	{
		return converter;
	}
	
	public Class getDestination()
	{
		return destination;
	}
	
	public Annotation getSepcifiers()
	{
		return specifier;
	}
	
	public Annotation[] getEnvironment()
	{
		return environment;
	}
	
	public Exception getCause()
	{
		return (Exception)super.getCause();
	}
}
