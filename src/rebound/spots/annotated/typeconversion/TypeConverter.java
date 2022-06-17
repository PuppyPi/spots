/*
 * Created on Jun 2, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated.typeconversion;

import java.lang.annotation.Annotation;
import rebound.spots.ActionBeanContext;

/**
 * A type converter is a stateless object which converts raw form data into values which can be bound to instance variables.<br>
 * See {@link ConverterSpecifier} on how specifier annotations work.<br>
 * @author RProgrammer
 */

//Todo make more type converters (str(obj,str,c[C]har,c[C]har[],build,buff,seq), bool, num(+bigintdec+Number), enum, date, filebean)

//Note to self:
//		If a typeconverter needs to choose its source (ie a filtered Tc) based the destination it will be requesting of the source, I can create a new switchboard TypeConverter which will hold an associative array of destination Classes to specifier Annotations

public interface TypeConverter<Specifier extends Annotation>
{
	/**
	 * Statelessly convert request data into an object to give a bound method.<br>
	 * @param context The request data
	 * @param destination The class the result of this method will be casted to (you may give something else if this is not supported, which will result in a {@link ClassCastException})
	 * @param specifier The specifier annotation
	 * @param environment The environment. (Surrounding annotations; Used for chaining)
	 * @return The value of the bound member as determined by the information given
	 * @throws Exception Any exception which occurs during conversion. (eg, If <code>null</code> is not allowed throw a {@link NullPointerException})
	 */
	public <E> E convert(ActionBeanContext context, Class<E> destination, Specifier specifier, Annotation[] environment) throws Exception;
	
	/**
	 * Tests if a given class is a valid parameter to <code>destination</code> of {@link #convert(ActionBeanContext, Class, Annotation)}.<br>
	 */
//	public boolean isDestinationSupported(Class destination);
}
