/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.util.binding.annotated.typeconversion.typeconverters.scalar;

import static rebound.util.collections.BasicCollectionUtilities.*;
import java.lang.annotation.Annotation;
import java.util.List;
import rebound.server.fileupload.FileValue;
import rebound.spots.ActionBeanContext;
import rebound.spots.traditionaljee.util.fileupload.MultipartFilter;
import rebound.spots.util.binding.annotated.typeconversion.TypeConverter;

public class FilesValueTypeConverter
implements TypeConverter<FormBoundFiles>
{
	public Object convert(ActionBeanContext context, Class destination, FormBoundFiles specifier, Annotation[] environment) throws Exception
	{
		Class expectedSuperType = specifier.singletonOnly() ? FileValue.class : List.class;
		if (!expectedSuperType.isAssignableFrom(destination))
			throw new ClassCastException();
		
		List<FileValue<?>> filesvalue = (List<FileValue<?>>) context.getRequest().getAttribute(MultipartFilter.STORAGE_ATTRIBUTE_PREFIX+specifier.value());
		
		if (specifier.singletonOnly())
			return getSingleElementOrNullIfNone(filesvalue);  //the AcceptFilter should have made sure more than one file wasn't allowed
		else
			return filesvalue;
	}
}
