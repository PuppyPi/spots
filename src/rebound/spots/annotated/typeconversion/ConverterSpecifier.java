/*
 * Created on Jun 2, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated.typeconversion;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NOTE: This annotation does not apply to bound fields.<br>
 * It applies to an annotation which bound fields are annotated with. The annotation which has {@link ConverterSpecifier} is called a Specifier annotation.<br>
 * <br>
 * An annotation custom converter specifiers must have.<br>
 * Specification of a converter works like this:<br>
 * A field has some arbitrary annotation (only one).<br>
 * The annotation has {@link ConverterSpecifier} with <code>type()</code> set to the corresponding converter type.<br>
 * When the field is bound, the type converter specified in <code>type()</code> is instantiated and the arbitrary annotation is passed to its {@link TypeConverter#convert(rebound.spots.ActionBeanContext, Class, Annotation, Annotation[]) convert()} method.<br>
 * <br>
 * Note: If you specify a type converter which is not expecting to accept the target annotation, you will be in some serious gamut.<br>
 * Note: Just to be sure you understand, every concrete {@link TypeConverter} implementation must have a corresponding annotation (specifier) which is annotated with this class.<br>
 * @author RProgrammer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public abstract @interface ConverterSpecifier
{
	/**
	 * The class of a {@link TypeConverter} which {@link TypeConverter#convert(rebound.spots.ActionBeanContext, Class, Annotation, Annotation[]) accepts} the target annotation.<br>
	 */
	Class<? extends TypeConverter> value();
	
	/**
	 * The name of the annotation field which will be used as its index in the environment.<br>
	 */
	String indexField() default "i";
}
