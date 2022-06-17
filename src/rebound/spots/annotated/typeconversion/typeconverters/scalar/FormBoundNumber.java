/*
 * Created on Jun 5, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import rebound.spots.annotated.typeconversion.ConverterSpecifier;

/**
 * Specifier to {@link NumericFormTypeConverter}.<br>
 * @author RProgrammer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@ConverterSpecifier(NumericFormTypeConverter.class)
public @interface FormBoundNumber
{
	/**
	 * The form field to read from.<br>
	 */
	String value();
	
	/**
	 * If this is <code>true</code>, the following result in an exception during type conversion:<br>
	 * Nonexistant field: {@link NullPointerException}<br>
	 * Invalid data: {@link NumberFormatException}<br>
	 * <br>
	 * If this is <code>false</code>, type conversion will always succeed (regardless of form data, if you provide incorrect class or specifier that will still break).<br>
	 * Instead of throwing an exception, nonstrict mode will bind to 0 (in what ever type you specify).<br>
	 */
	boolean strict() default false;
	
	/**
	 * The base to parse as.<br>
	 * Valid values are  Character.MIN_RADIX(2) ≤ radix ≤ Character.MAX_RADIX(36).<br>
	 * <br>
	 * Note: This currently does not work on Float, Double, and BigDecimal
	 */
	int radix() default 10;
	
	
	int i() default 0;
}
