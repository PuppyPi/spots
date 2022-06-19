/*
 * Created on Jun 6, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util.binding.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.Annotation;
import rebound.spots.ActionBeanContext;
import rebound.spots.util.binding.annotated.typeconversion.TypeConverter;

/**
 * Simply takes textual parameters out of the form and puts them into bindings.<br>
 * <br>
 * Supports: String,Object,CharSequence, StringBuffer,StringBuilder,char,Character
 * Specifier: {@link FormBound}<br>
 * Throws: {@link NullPointerException} if {@link FormBound#require()} is <code>true</code> and the form parameter wasn't sent (or dest=char,Char and it's an empty string)<br>
 * @author RProgrammer
 */
public class FormTypeConverter
implements TypeConverter<FormBound>
{
	public Object convert(ActionBeanContext context, Class dest, FormBound specifier, Annotation[] environment) throws Exception
	{
		if (dest.isArray())
		{
			String[] data = context.getRequest().getParameterValues(specifier.value());
			
			if (data == null)
			{
				if (specifier.require())
					throw new NullPointerException();
				else
					return null;
			}
			else
			{
				Class destComponentType = dest.getComponentType();
				
				if (destComponentType == String.class || destComponentType == CharSequence.class || destComponentType == Object.class)
					return data;
				
				else
					throw new ClassCastException();
			}
		}
		else
		{
			String data = context.getRequest().getParameter(specifier.value());
			
			if (data == null)
			{
				if (specifier.require())
					throw new NullPointerException();
				else
					return null;
			}
			else
			{
				if (dest == String.class || dest == CharSequence.class || dest == Object.class)
					return data;
				else if (dest == StringBuffer.class)
					return new StringBuffer(data);
				else if (dest == StringBuilder.class)
					return new StringBuilder(data);
				
				else if (dest == char.class || dest == Character.class)
				{
					if (data.length() == 0)
					{
						if (specifier.require())
							throw new NullPointerException();
						else
						{
							if (dest == char.class)
								return (char)0;
							else //Character
								return null;
						}
					}
					else
					{
						return data.charAt(0);
					}
				}
				
				else
					throw new ClassCastException();
			}
		}
	}
	
	
	
	public boolean isDestinationSupported(Class destination)
	{
		return
		destination.isArray()
		&&
		(
			destination.getComponentType() == Object.class ||
			destination.getComponentType() == CharSequence.class ||
			destination.getComponentType() == String.class
		)
		||
		!destination.isArray()
		&&
		(
			destination == Object.class ||
			destination == CharSequence.class ||
			destination == String.class ||
			destination == StringBuffer.class ||
			destination == StringBuilder.class ||
			destination == char.class ||
			destination == Character.class
		);
	}
}
