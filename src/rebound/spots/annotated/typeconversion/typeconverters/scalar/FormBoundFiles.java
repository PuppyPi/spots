/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import rebound.spots.annotated.typeconversion.ConverterSpecifier;
import rebound.spots.util.fileupload.AcceptFilter;
import rebound.spots.util.fileupload.MultipartFilter;

/**
 * Note: This must be used in conjunction with {@link MultipartFilter}.<br>
 * @author Sean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@ConverterSpecifier(FilesValueTypeConverter.class)
public @interface FormBoundFiles
{
	/**
	 * The name of the form field to intercept files from.<br>
	 */
	String value();
	
	
	/**
	 * If only one file is acceptable.<br>
	 * This is intended to be used by {@link AcceptFilter}s, and is ignored by the {@link FilesValueTypeConverter}.<br>
	 */
	boolean singletonOnly() default false;
	
	
	
	
	
	
	//Legacy code from the nonexistant future
	int i() default 0;
}
