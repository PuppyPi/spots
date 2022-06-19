/*
 * Created on Jun 11, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util.binding.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import rebound.spots.util.binding.annotated.typeconversion.ConverterSpecifier;


/**
 * Specifier to {@link BooleanFormTypeConverter}.<br>
 * @author RProgrammer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@ConverterSpecifier(BooleanFormTypeConverter.class)
public @interface FormBoundBoolean
{
	/**
	 * The form-field name to bind from.<br>
	 */
	String value();
	
	
	/**
	 * A list of string values which will result in a <code>true</code> evaluation, everything else will result in <code>false</code>.<br>
	 * If this has at least one entry, {@link FormBoundBoolean#falses() falses} is ignored.<br>
	 */
	String[] trues() default {"true", "t", "yes", "y", "on", "1"};
	
	/**
	 * If {@link FormBoundBoolean#trues() trues} is empty, the {@link FormBoundBoolean#value() form field} is compared to these, and if it matches any one it evaluates to <code>false</code>, anything else will evaluate to <code>true</code>.<br>
	 */
	String[] falses() default {"", "0"};
	
	/**
	 * Whether to compare {@link FormBoundBoolean#trues() trues} or {@link FormBoundBoolean#falses() falses} case-insensitively.<br>
	 */
	boolean ci() default true;
	
	
	int i() default 0;
}
