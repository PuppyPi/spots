/*
 * Created on Jun 2, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util.binding.annotated;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import rebound.spots.util.binding.annotated.BindingException.Type;


/**
 * This filtering mechanism allows for the spreading out of error handler logic into multiple methods (as opposed to one {@link AnnotatedActionBean#handleBindingError(BindingException)}.<br>
 * The target method must have a signature of: <code>public void <i>handle</i>({@link BindingException})</code><br>
 * Methods may have {@link HandlesBindingError more than one} of this annotation, and methods can have these overlapping.<br>
 * If there are overlapping methods, the following order of precedence is used to determine which is called (because only one is called).<br>
 * <ul>
 * 	<li>{@link HandlesBindingError#name() Name}</li>
 * 	<li>{@link HandlesBindingError#type() Type}</li>
 *	<li>{@link HandlesBindingError#excClass() Exception class}</li>
 * </ul>
 * That is to say that if there are methods which have the name set to the field {@link BindingException#getBoundMethod()}.{@link Method#getName() getName()} in question, handlers without a name set are ignored.<br>
 * And likewise, those with the type set correctly win out (All three {@link Type}s is the catchall).<br>
 * Ditto for the exception class (<code>Exception.class</code> is the catchall).<br>
 * If two methods have the same combination of name type and exception class, it is undefined which one will handle the error.<br> 
 * @author RProgrammer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface HandlesBindingError
{
	/**
	 * The name of the problematic bound method (which means if you bind a field you must provide the setter).<br>
	 * If it is <code>""</code> (<b>default</b>), it matches any form field.<br>
	 */
	String[] name() default "";
	
	/**
	 * The point at which the exception occurred must be in this array to be handled by the target method.<br>
	 */
	BindingException.Type[] type() default {Type.TYPECONVERSION, Type.INVOCATION};
	
	/**
	 * The highest acceptable class of exception.<br>
	 * {@link BindingException Wrapped} exceptions will be of or subclasses of this type.<br> 
	 */
	Class[] excClass() default Exception.class;
}
