/*
 * Created on Jun 2, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import rebound.spots.annotated.typeconversion.TypeConverterManager;

/**
 * This class wraps up an error which occurred while binding a method to a form field.<br>
 * These exceptions are the sole value passed to handlers and contain everything the actionbean should need to provide a message to the client.<br>
 * <br>
 * {@link Type}s:
 * <ul>
 * 	<li>TYPECONVERSION - Error occurred during {@link TypeConverterManager#convert(rebound.spots.ActionBeanContext, Class, Annotation, Annotation[])}</li>
 * 	<li>INVOCATION - Problem occurred during reflective invocation of the setter</li>
 * 	<li>TARGET - The setter itself threw an Exception (if it throws an Error or Throwable, it is not wrapped in {@link BindingException})</li>
 * </ul>
 * @author RProgrammer
 */
public class BindingException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public static enum Type
	{
		TYPECONVERSION,
		INVOCATION,
		TARGET,
	}
	
	
	protected Type type;
	protected Annotation specifier;
	protected Method bound;
	
	public BindingException()
	{
		super();
	}
	
	public BindingException(Method bound, Type type, Annotation specifier, Exception exception)
	{
		super("Binding exception in "+type+", on method "+bound+", specified by "+specifier, exception);
		this.type = type;
		this.specifier = specifier;
		this.bound = bound;
	}
	
	/**
	 * If the FooBound annotation is to a field, this returns the JavaBeans setter actually used.<br>
	 */
	public Method getBoundMethod()
	{
		return this.bound;
	}
	
	public Annotation getSpecifier()
	{
		return this.specifier;
	}
	
	public Type getType()
	{
		return this.type;
	}
	
	public Exception getException()
	{
		return (Exception)getCause();
	}
}
