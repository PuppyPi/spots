/*
 * Created on Jun 11, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.Annotation;
import rebound.spots.ActionBeanContext;
import rebound.spots.annotated.typeconversion.TypeConverter;

public class BooleanFormTypeConverter
implements TypeConverter<FormBoundBoolean>
{
	public Object convert(ActionBeanContext context, Class destination, FormBoundBoolean specifier, Annotation[] environment) throws Exception
	{
		if (destination != boolean.class && destination != Boolean.class && destination != Object.class)
			throw new ClassCastException();
		
		String data = context.getRequest().getParameter(specifier.value());
		
		if (data == null)
			return destination == boolean.class ? false : null;
		
		if (specifier.trues().length > 0)
		{
			for (String t : specifier.trues())
				if ((specifier.ci() && data.equalsIgnoreCase(t)) || (!specifier.ci() && data.equals(t)))
					return true;
			return false;
		}
		else
		{
			for (String f : specifier.falses())
				if ((specifier.ci() && data.equalsIgnoreCase(f)) || (!specifier.ci() && data.equals(f)))
					return false;
			return true;
		}
	}
	
	public boolean isDestinationSupported(Class destination)
	{
		return
		destination == Object.class ||
		destination == boolean.class ||
		destination == Boolean.class;
	}
}
