/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.Annotation;
import java.util.List;
import rebound.spots.ActionBeanContext;
import rebound.spots.annotated.typeconversion.TypeConverter;
import rebound.spots.util.MultipartFilter;

public class FilesValueTypeConverter
implements TypeConverter<FormBoundFiles>
{
	public Object convert(ActionBeanContext context, Class destination, FormBoundFiles specifier, Annotation[] environment) throws Exception
	{
		if (!List.class.isAssignableFrom(destination))
			throw new ClassCastException();
		
		return context.getRequest().getAttribute(MultipartFilter.STORAGE_ATTRIBUTE_PREFIX+specifier.value());
	}
}
