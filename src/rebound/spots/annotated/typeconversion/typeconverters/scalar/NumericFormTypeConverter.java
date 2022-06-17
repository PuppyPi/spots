/*
 * Created on Jun 5, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated.typeconversion.typeconverters.scalar;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import rebound.spots.ActionBeanContext;
import rebound.spots.annotated.typeconversion.TypeConverter;
import rebound.text.StringUtilities;
import rebound.util.AngryReflectionUtility;

/**
 * Converts basic numeric form field data (currently can't handle multipart data).<br>
 * Supports: byte,Byte,short,Short,int,Integer,long,Long,float,Float,double,Double,BigInteger,BigDecimal,Number
 * Specifier: {@link FormBoundNumber}<br>
 * Throws:<br>
 * <ul>
 * 	<li>{@link NumberFormatException} if {@link FormBoundNumber#strict() strict} and the data was an invalid number for the type given.</li>
 * 	<li>{@link NullPointerException} if {@link FormBoundNumber#strict() strict} and the form parameter was omitted (<code>null</code>)</li>
 * </ul>
 * @author RProgrammer
 */
public class NumericFormTypeConverter
implements TypeConverter<FormBoundNumber>
{
	public Object convert(ActionBeanContext context, Class destination, FormBoundNumber specifier, Annotation[] environment) throws Exception
	{
		boolean strict = specifier.strict();
		int radix = specifier.radix();
		
		String data = context.getRequest().getParameter(specifier.value());
		
		
		if (data == null)
		{
			if (strict)
				throw new NullPointerException();
			else
				return makeZero(destination);
		}
		else
		{
			//Strict.
			if (strict)
			{
				if (destination == byte.class || destination == Byte.class)
					return Byte.parseByte(data, radix);
				else if (destination == short.class || destination == Short.class)
					return Short.parseShort(data, radix);
				else if (destination == int.class || destination == Integer.class)
					return Integer.parseInt(data, radix);
				else if (destination == long.class || destination == Long.class)
					return Long.parseLong(data, radix);
				else if (destination == float.class || destination == Float.class)
					return Float.parseFloat(data);
				else if (destination == double.class || destination == Double.class)
					return Double.parseDouble(data);
				
				else if (destination == BigInteger.class)
					return new BigInteger(data, radix);
				else if (destination == BigDecimal.class)
					return new BigDecimal(data);
				
				else if (destination == Number.class || destination == Object.class)
				{
					try
					{
						return Long.parseLong(data, radix);
					}
					catch (NumberFormatException exc)
					{
						return Double.parseDouble(data);
					}
				}
				
				else
					throw new ClassCastException();
			}
			
			
			//Lenient.
			else
			{
				//Floating point (Hey, gimme a break! I haven't got around to making a lenient float parser)
				{
					try
					{
						if (destination == float.class || destination == Float.class)
							return Float.parseFloat(data);
						else if (destination == double.class || destination == Double.class)
							return Double.parseDouble(data);
					}
					catch (NumberFormatException exc)
					{
						return makeZero(destination);
					}
				}
				
				//Integer
				{
					if (destination == long.class || destination == Long.class)
						return StringUtilities.parseLongLeniently(data, radix, 0L);
					else if (destination == byte.class || destination == Byte.class || destination == short.class || destination == Short.class || destination == int.class || destination == Integer.class || destination == Number.class)
						return AngryReflectionUtility.cast(StringUtilities.parseIntegerLeniently(data, radix, 0), destination);
					else
						throw new ClassCastException();
				}
			}
		}
	}
	
	
	
	
	protected static Object makeZero(Class destination)
	{
		if (destination == byte.class)
			return (byte)0;
		else if (destination == short.class)
			return (short)0;
		else if (destination == int.class)
			return (int)0;
		else if (destination == long.class)
			return (long)0;
		else if (destination == float.class)
			return (float)0;
		else if (destination == double.class)
			return (double)0;
		
		else if
		(
			destination == Byte.class ||
			destination == Short.class ||
			destination == Integer.class ||
			destination == Long.class ||
			destination == Float.class ||
			destination == Double.class ||
			destination == BigInteger.class ||
			destination == BigDecimal.class ||
			destination == Number.class
		)
			return null;
		
		else
			throw new ClassCastException();
	}
	
	
	public boolean isDestinationSupported(Class destination)
	{
		return
		destination == Object.class ||
		destination == Number.class ||
		destination == byte.class ||
		destination == Byte.class ||
		destination == short.class ||
		destination == Short.class ||
		destination == int.class ||
		destination == Integer.class ||
		destination == long.class ||
		destination == Long.class ||
		destination == float.class ||
		destination == Float.class ||
		destination == double.class ||
		destination == Double.class ||
		destination == BigInteger.class ||
		destination == BigDecimal.class;
	}
}
