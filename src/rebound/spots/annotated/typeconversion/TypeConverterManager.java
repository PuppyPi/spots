/*
 * Created on Jun 2, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated.typeconversion;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import rebound.spots.ActionBeanContext;

/**
 * This class implements a static, thread-safe pool of type converters (one per concrete class, since they are stateless) and some utility methods.<br>
 * @author RProgrammer
 */
public class TypeConverterManager
{
	protected static HashMap<Class, TypeConverter> map = new HashMap<Class, TypeConverter>();
	
	
	
	
	
	/**
	 * Converts data in the {@link ActionBeanContext} into an instance of the given <code>destination</code>, based on the binding specifier.<br>
	 * @param context The web context.
	 * @param specifier The converter specifier
	 * @throws IllegalArgumentException If <code>specifier</code> is not a converter specifier.
	 */
	public static <E> E convert(ActionBeanContext context, Class<E> destination, Annotation specifier, Annotation[] environment) throws IllegalArgumentException, TypeConversionException, TypeConverterNotFoundException
	{
		Class<? extends TypeConverter> converterClass = getSpecified(specifier);
		
		if (converterClass == null)
			throw new IllegalArgumentException();
		
		TypeConverter converter = getTypeConverter(converterClass);
		
		if (converter == null)
			throw new TypeConverterNotFoundException(converterClass);
		
		try
		{
			return (E)converter.convert(context, destination, specifier, environment);
		}
		catch (Exception exc)
		{
			throw new TypeConversionException(converter, destination, specifier, environment, exc);
		}
	}
	
	
	
	/**
	 * Tests if an Annotation is a specifier to a typeconverter.<br>
	 */
	public static boolean isSpecifier(Annotation a)
	{
		return getSpecified(a) != null;
	}
	
	
	
	/**
	 * Gets the {@link TypeConverter} the given annotation specifies, or <code>null</code> if it doesn't.<br>
	 */
	public static Class<? extends TypeConverter> getSpecified(Annotation o)
	{
		if (o != null && o instanceof Annotation)
		{
			Annotation a = (Annotation)o;
			
			ConverterSpecifier specifierTag = a.annotationType().getAnnotation(ConverterSpecifier.class);  //Annotated annotation :)
			
			if (specifierTag == null)
				return null;
			else
				return specifierTag.value();
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	/**
	 * Fetches or instantiates an instance of the given class of TypeConverter.<br>
	 */
	public static <T extends TypeConverter> T getTypeConverter(Class<T> clazz)
	{
		if (clazz == null)
			return null;
		
		if (Modifier.isAbstract(clazz.getModifiers()))
		{
			System.err.println("Cannot instantiate abstract 'type converter': "+clazz);
			return null;
		}
		
		T converter = null;
		{
			synchronized (map)
			{
				converter = (T)map.get(clazz);
				
				if (converter == null)
				{
					try
					{
						converter = clazz.newInstance();
					}
					catch (InstantiationException exc)
					{
						System.err.println("Error occurred while instantiating alleged type converter "+clazz+":");
						System.err.println(exc.getClass()+": \""+exc.getMessage()+"\"");
						exc.printStackTrace();
						if (exc.getCause() != null)
						{
							System.err.println("Cause:");
							System.err.println(exc.getCause().getClass()+": \""+exc.getCause().getMessage()+"\"");
							exc.getCause().printStackTrace();
						}
					}
					catch (IllegalAccessException exc)
					{
						System.err.println("IllegalAccessException occurred while instantiating alleged type converter "+clazz+":");
						System.err.println("Message: \""+exc.getMessage()+"\"");
						exc.printStackTrace();
						if (exc.getCause() != null)
						{
							System.err.println("Cause:");
							System.err.println(exc.getCause().getClass()+": \""+exc.getCause().getMessage()+"\"");
							exc.getCause().printStackTrace();
						}
					}
					
					if (converter != null)
						map.put(clazz, converter);
				}
			}
		}
		
		return converter;
	}
	
	
	
	public static int getSpecifierIndex(Annotation a)
	{
		try
		{
			Class c = a.annotationType();
			ConverterSpecifier tag = (ConverterSpecifier)c.getAnnotation(ConverterSpecifier.class);
			String name = tag.indexField();
			Method property = c.getDeclaredMethod(name);
			return (Integer)property.invoke(a);
		}
		catch (IllegalArgumentException exc)
		{
			return -1;
		}
		catch (IllegalAccessException exc)
		{
			return -1;
		}
		catch (ClassCastException exc)
		{
			return -1;
		}
		catch (SecurityException exc)
		{
			return -1;
		}
		catch (InvocationTargetException exc)
		{
			return -1;
		}
		catch (NoSuchMethodException exc)
		{
			return -1;
		}
	}
}
