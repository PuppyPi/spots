/*
 * Created on Jun 6, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import rebound.spots.annotated.typeconversion.ConverterSpecifier;

/**
 * Specifier to {@link FormTypeConverter}.<br>
 * @author RProgrammer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@ConverterSpecifier(FormTypeConverter.class)
public @interface FormBound
{
	/**
	 * The name of the form parameter to bind from.<br>
	 */
	String value();
	
	/**
	 * If the form parameter must be sent (if this is <code>true</code>, the binding will never be <code>null</code>, or a {@link NullPointerException} will be thrown before binding).<br>
	 */
	boolean require() default false;
	
	int i() default 0;
}
