/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util.binding.annotated;

import static java.util.Objects.*;
import static rebound.text.StringUtilities.*;
import static rebound.util.AngryReflectionUtility.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import rebound.annotations.hints.ImplementationTransparency;
import rebound.spots.ActionBean;
import rebound.spots.util.AbstractActionBean;
import rebound.spots.util.binding.annotated.typeconversion.ConverterSpecifier;
import rebound.spots.util.binding.annotated.typeconversion.TypeConversionException;
import rebound.spots.util.binding.annotated.typeconversion.TypeConverter;
import rebound.spots.util.binding.annotated.typeconversion.TypeConverterManager;
import rebound.spots.util.binding.annotated.typeconversion.TypeConverterNotFoundException;
import rebound.spots.util.binding.annotated.typeconversion.typeconverters.scalar.FormBound;

/**
 * This is the <code>AnnotatedActionBean</code>.<br>
 * If you make an {@link ActionBean} which extends this, code here will allow you to use annotations in your action bean class to make your life easier.<br>
 * <br>
 * There are two main blocks an action is split into here:<br>
 * {@link AnnotatedActionBean#doBinding() Binding} and {@link AnnotatedActionBean#doLogic() Logic}.<br>
 * <br>
 * Logic is the simpler of the two. The only thing this class helps you with in that is determining events.<br>
 * An event handler method is annotated with one of two annotations: {@link HandlesEvent} or {@link DefaultHandler}.<br>
 * The mechanisms here make extensive use of overriding.
 * For example: If you only have one event, instead of only marking one method with {@link DefaultHandler}, just override {@link AnnotatedActionBean#doLogic() doLogic()}! (and don't call super())<br>
 * Note: Event handler methods must be public and have no args.<br>
 * <br>
 * Binding however, is more complex.<br>
 * Unlike Stripes, this framework does not handle validation errors.<br>
 * In fact it really doesn't explicitly 'validate', just enough to convert types.<br>
 * Ok, the binding system works like this:<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ For every {@link FormBound} field or method do this:<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ Find the corresponding form field (as given by {@link FormBound#value()}, continue only if it exists.<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ If it does not exist and it is {@link FormBound#require()}, throw a {@link RequiredFieldNotFoundException} to the handler —— TODO it does not do this X'DD<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ Determine the correct {@link TypeConverter} ({@link ConverterSpecifier specifier} given or supported type)<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ Convert to the type (and ensure that is is indeed converted to the correct type)<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ Catch any {@link Exception}s and handle them (see below). Continue only if there are none<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ If {@link FormBound}'s target is an instance field, set the JavaBean setter<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ If {@link FormBound}'s target is a method with a one arg signature, invoke it<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ Catch any {@link Exception}s thrown by the method (doesn't apply to fields) and handle them (see below)<br><br>
 * <br>
 * <br>
 * Binding error handling:<br>
 * When an error occurs during binding, it is {@link BindingException bundled} and sent to the {@link AnnotatedActionBean#handleBindingError(BindingException)} method.<br>
 * The default behavior of this method (which is, unless you override it) is to scan the subclass's methods for the {@link HandlesBindingError} (or {@link HandlesBindingError}) annotations.<br>
 * It then determines the most specific handling method and invokes it (the method must take one {@link BindingException} argument).<br>
 * After that the {@link AnnotatedActionBean#doBinding()} method returns <code>false</code>, meaning the {@link AnnotatedActionBean#doLogic() logic} is not invoked.<br>
 */
public abstract class AnnotatedActionBean
extends AbstractActionBean
{
	public void doAction() throws ServletException, IOException
	{
		boolean bindingSuccessful = false;
		
		long startBinding = System.currentTimeMillis();
		bindingSuccessful = doBinding();
		logBenchmark(startBinding, "Binding for " + getClass());
		
		if (bindingSuccessful)
		{
			long startLogic = System.currentTimeMillis();
			doLogic();
			
			logBenchmark(startLogic, "Logic for "+getClass()+" on "+getRequest().getRequestURI());
		}
	}
	
	
	
	//<Binding
	/**
	 * Do the preliminary step of binding form variables to instance fields.<br>
	 * If binding is not successful, this method should put something up for the user and return <code>false</code>.<br>
	 * If it <i>is</i> successful, it should just return <code>true</code>.<br>
	 * <br>
	 * Overrider note: If you want to perform some generic binding of you own, you can override this method and call <code>super.doBinding()</code> if there are some normal annotated bindings you still want me to do for you.<br>
	 * @return If binding was successful and the logic should be invoked normally.
	 */
	protected boolean doBinding() throws ServletException, IOException
	{
		Binding[] bindings = getBindings();
		
		//Enumerate bound methods
		for (Binding binding : bindings)
		{
			log("Type converting "+binding.getMethod()+" which accepts a "+binding.getDestination()+" using typeconverter "+TypeConverterManager.getSpecified(binding.getSpecifier()), true);
			
			Object converted = null;
			
			
			//Type convert into converted
			{
				try
				{
					converted = TypeConverterManager.convert(getContext(), binding.getDestination(), binding.getSpecifier(), binding.getEnvironment());
				}
				catch (TypeConversionException exc)
				{
					handleBindingError(new BindingException(binding.getMethod(), BindingException.Type.TYPECONVERSION, binding.getSpecifier(), exc));
					return false;
				}
				catch (TypeConverterNotFoundException exc)
				{
					handleBindingError(new BindingException(binding.getMethod(), BindingException.Type.TYPECONVERSION, binding.getSpecifier(), exc));
					return false;
				}
			}
			
			
			
			//Invoke the method with converted
			{
				try
				{
					binding.getMethod().invoke(this, new Object[]{converted});
				}
				catch (IllegalArgumentException exc)
				{
					handleBindingError(new BindingException(binding.getMethod(), BindingException.Type.INVOCATION, binding.getSpecifier(), exc));
					return false;
				}
				catch (IllegalAccessException exc)
				{
					handleBindingError(new BindingException(binding.getMethod(), BindingException.Type.INVOCATION, binding.getSpecifier(), exc));
					return false;
				}
				catch (InvocationTargetException exc)
				{
					if (exc.getCause() == null || exc.getCause() instanceof Exception)
						handleBindingError(new BindingException(binding.getMethod(), BindingException.Type.TARGET, binding.getSpecifier(), (Exception)exc.getCause()));
					else if (exc.getCause() instanceof Error)
						throw (Error)exc.getCause();
					else
						throw new ServletException(exc.getCause());
				}
			}
		}
		
		//Everything bound ok, phew!
		return true;
	}
	
	
	
	
	
	
	//<Errors...
	/**
	 * Invoked for every binding error (type conversion and invocation).<br>
	 * Override this if you want to handle every error in only one method.<br>
	 * @param exc The bundled up exception
	 */
	protected void handleBindingError(BindingException exc) throws ServletException, IOException
	{
		//Dole out the exception to underling handlers
		
		Method handler = findBindingErrorHandler(exc);
		
		if (handler != null)
		{
			try
			{
				handler.invoke(this, new Object[]{exc});
			}
			catch (IllegalArgumentException exc1)
			{
				throw new ServletException("Binding error handler "+handler+" does not have the signature \"public void foo(BindingException exc)\"", exc1);
			}
			catch (IllegalAccessException exc1)
			{
				throw new ServletException(exc1);
			}
			catch (InvocationTargetException exc1)
			{
				throw new ServletException(exc1.getCause());
			}
		}
		else
		{
			throw new ServletException(exc);
		}
	}
	
	
	
	
	protected Method findBindingErrorHandler(BindingException exc)
	{
		long start = System.currentTimeMillis();
		//Search for the most specific error handler
		//	+ A handler is only non-specific if it matches all of an attribute, filters which match broadly are no less specific than those which match less narrowly
		
		
		/*
		 * Storage:
		 * 	Name:
		 * 		0 = name specified
		 * 		1 = name nonspec
		 * 	
		 * 	Type:
		 * 		0 = namespec, typespec
		 * 		1 = namespec, typenon
		 * 		2 = namenon, typespec
		 * 		3 = namenon, typenon
		 * 	
		 * 	Class:
		 * 		0 = namespec, typespec, classspec //Note: this is never used, anything which would be set here will be returned instantly because it is the most specific
		 * 		1 = namespec, typespec, classnon
		 * 		2 = namespec, typenon, classspec
		 * 		3 = namespec, typenon, classnon
		 * 		4 = namenon, typespec, classspec
		 * 		etc.
		 * 	
		 * 	With specified=0 and nonspec=1, indices are the
		 * 	 binary number by the specificity of the lowest
		 * 	 attribute being the lowest bit.
		 * 
		 * This makes the first of each array the most specific and the last the least specific. (Following the order of precedence set out in HandlesBindingError.java)
		 */
		
		Method[][] handlers_Name = new Method[2][];
		Method[][] handlers_Type = new Method[4][];
		Method[]   handler_Class = new Method[8]  ; //This is singular because only one of the end resulting handlers is returned
		int[] counts_Type = new int[4];
		
		//Initialize them
		{
			//Delegate responsibility of finding handlers by name to other methods so they can implement tricks to improve performance (like caching)
			handlers_Name[0] = getNameSpecificBindingErrorHandlers(exc.getBoundMethod().getName());
			handlers_Name[1] = getNameNonspecificBindingErrorHandlers();
			
			handlers_Type[0] = new Method[handlers_Name[0].length];
			handlers_Type[1] = new Method[handlers_Name[0].length];
			handlers_Type[2] = new Method[handlers_Name[1].length];
			handlers_Type[3] = new Method[handlers_Name[1].length];
		}
		
		
		
		
		
		//Copy handlers which match the correct specificity to each slot
		//Work by Type
		for (int nameIndex = 0; nameIndex < 2; nameIndex++)
		{
			Method[] handlers = handlers_Name[nameIndex];
			
			for (Method handler : handlers)
			{
				boolean passes = false;
				boolean nonspecific = false;
				
				
				//Does the filter pass?
				//and is it nonspecific? (regarding type())
				{
					HandlesBindingError filter = handler.getAnnotation(HandlesBindingError.class);
					if (filter != null)
					{
						nonspecific = (filter.type().length == BindingException.Type.values().length);
						
						//Calculate passes
						if (nonspecific)
						{
							//A non-specific filter will always pass!
							passes = true;
						}
						else
						{
							//Does this specific filter specify our type?
							for (int typeIndex = 0; typeIndex < filter.type().length && !passes; typeIndex++)
								if (filter.type()[typeIndex] == exc.getType())
									passes = true;
						}
					}
				}
				
				
				
				
				//If the handler passes, record its passing
				if (passes)
				{
					int typeIndex = (nameIndex << 1) | (nonspecific ? 1 : 0);
					handlers_Type[typeIndex][counts_Type[typeIndex]] = handler;
					counts_Type[typeIndex]++;
				}
			}
		}
		
		
		
		
		
		//Copy the handler which matches the correct specificity to each slot
		//Work by Exception Class
		for (int typeIndex = 0; typeIndex < 4; typeIndex++)
		{
			Method[] handlers = handlers_Type[typeIndex];
			
			for (Method handler : handlers)
			{
				boolean passes = false;
				boolean nonspecific = false;
				
				
				//Does the filter pass?
				//and is it nonspecific? (regarding excClass())
				{
					HandlesBindingError filter = handler.getAnnotation(HandlesBindingError.class);
					if (filter != null)
					{
						
						//Calculate specificity
						for (int classIndex = 0; classIndex < filter.excClass().length && !nonspecific; classIndex++)
							if (filter.excClass()[classIndex] == Exception.class)
								nonspecific = true;
						
						//Calculate passes
						if (nonspecific)
						{
							//A non-specific filter will always pass!
							passes = true;
						}
						else
						{
							for (int classIndex = 0; classIndex < filter.excClass().length && !passes; classIndex++)
								if (filter.excClass()[classIndex].isAssignableFrom(exc.getClass()))
									passes = true;
						}
					}
				}
				
				
				
				//If the handler passes, record its passing
				if (passes)
				{
					if (typeIndex == 0 && !nonspecific)
					{
						logBenchmark(start, "Finding perfect error handler for "+getClass());
						return handler; //Most specific needs has no competition
					}
					
					//Lesser specific handlers do not know if there is a more specific handler than them which has not been encountered yet, so they record themselves in the hope that no handler will be found which is more specific than they are (in which case they will win and be returned)
					int classIndex = (typeIndex << 1) | (nonspecific ? 1 : 0);
					handler_Class[classIndex] = handler; //Yes, this can overwrite others, but that's ok because there shoudn't be more than one anyway!
				}
			}
		}
		
		
		
		//Return most specific one
		{
			for (Method handler : handler_Class)
			{
				if (handler != null)
				{
					logBenchmark(start, "Finding error handler for "+getClass());
					return handler;
				}
			}
			
			logBenchmark(start, "Finding no error handler for "+getClass());
			return null;
		}
	}
	//	Binding>
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//	<Logic
	/**
	 * Do the actual logic after the binding has been done successfully.<br>
	 */
	protected void doLogic() throws ServletException, IOException
	{
		Method method = getEventHandler();
		
		if (method == null)
		{
			method = getDefaultEventHandler();
			log("Could not find specific event handler, defaulting to "+method, true);
		}
		
		if (method == null)
			throw new NullPointerException("Could not find default event handler for annotated actionbean "+getClass());
		
		if (method.getParameterTypes().length > 0)
			throw new IllegalStateException("Event handler method "+method+" has args, I can't work with that!");
		
		try
		{
			method.invoke(this, (Object[])null);
		}
		catch (IllegalArgumentException exc)
		{
			throw new ServletException(exc);
		}
		catch (IllegalAccessException exc)
		{
			throw new ServletException(exc);
		}
		catch (InvocationTargetException exc)
		{
			throw new ServletException(exc.getCause());
		}
	}
	
	/**
	 * Find a method which handles a sent event or null if you can't (don't worry about the defaulthandler).<br>
	 * <br>
	 * The default behavior of this is to check if any event handler's {@link HandlesEvent#value() event name} is a form parameter (regardless of its value).<br>
	 * This style of event detection is what happens when one constructs an HTML form with multiple submit buttons.<br>
	 */
	protected Method getEventHandler() throws ServletException, IOException
	{
		Method[] handlers = getEventHandlers();
		for (Method handler : handlers)
		{
			HandlesEvent a = handler.getAnnotation(HandlesEvent.class);
			if (a != null)
			{
				String[] events = a.value();
				
				for (String event : events)
					if (getRequest().getParameter(event) != null)
						return handler;
			}
		}
		return null;
	}
	//	Logic>
	
	
	
	
	
	
	
	
	
	//	<Introspection
	/**
	 * This provides you with a list of all {@link HandlesBindingError error handlers} for a given form field name
	 * @return An array of binding error handlers which handle errors from the given method ({@link HandlesBindingError#name()}) or an empty array or <code>null</code> if there are none
	 */
	protected Method[] getNameSpecificBindingErrorHandlers(String methodName)
	{
		Method[] handlers = getCache().nameSpecErrorHandlers.get(methodName);
		return handlers == null ? new Method[0] : handlers;
	}
	
	/**
	 * This provides you with every binding error handler (ie every method with {@link HandlesBindingError}).<br>
	 */
	protected Method[] getErrorHandlers()
	{
		return getCache().errorHandlers;
	}
	
	/**
	 * This provides you with a list of all 
	 */
	protected Method[] getNameNonspecificBindingErrorHandlers()
	{
		return getCache().nameNonSpecErrorHandlers;
	}
	
	/**
	 * Gets all event handlers.<br>
	 * @return An array of methods which should handle some events, can be empty, but never <code>null</code>
	 */
	protected Method[] getEventHandlers()
	{
		return getCache().eventHandlers;
	}
	
	/**
	 * Gets the default event handler or null.<br>
	 */
	protected Method getDefaultEventHandler() throws ServletException, IOException
	{
		return getCache().defaultEventHandler;
	}
	
	/**
	 * Examine the {@link FormBound} annotations and construct a list of methods from that.<br>
	 * For annotated fields, use their JavaBean setter.<br>
	 */
	protected Binding[] getBindings()
	{
		return getCache().bindings;
	}
	
	
	
	/**
	 * Maps methods to {@link FormBound}s and the specifier (useful because when fields are {@link FormBound}, the method used (the setter) doesn't have a {@link FormBound}).<br>
	 * @author RProgrammer
	 */
	public static class Binding
	{
		protected final Method method;
		protected final Annotation specifier;
		protected final Annotation[] environment;
		
		public Binding(Method method, Annotation specifier, Annotation[] environment)
		{
			this.method = method;
			this.specifier = specifier;
			this.environment = environment;
		}
		
		public Annotation getSpecifier()
		{
			return this.specifier;
		}
		
		public Method getMethod()
		{
			return this.method;
		}
		
		public Annotation[] getEnvironment()
		{
			return this.environment;
		}
		
		
		public Class getDestination()
		{
			Class[] p = getMethod().getParameterTypes();
			if (p.length > 0)
				return p[0];
			else
				return null;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//<Cache
	protected static HashMap<Class, CachedClass> map = new HashMap<Class, CachedClass>(32, 0.9f);
	
	public static class CachedClass
	{
		public Binding[] bindings;
		public HashMap<String, Method[]> nameSpecErrorHandlers;
		public Method[] nameNonSpecErrorHandlers;
		public Method[] errorHandlers;
		public Method[] eventHandlers;
		public Method defaultEventHandler;
	}
	
	public static CachedClass makeCacheFor(Class clazz)
	{
		//long start = System.currentTimeMillis();
		
		List<Binding> bindings = new ArrayList<Binding>();
		List<Method> errorHandlers = new ArrayList<Method>();
		List<Method> eventHandlers = new ArrayList<Method>();
		Method defaultEventHandler = null;
		
		
		//Make a pass through the fields to look for potential bindings
		List<String> fieldBindingSetterNames = new ArrayList<String>();
		List<Annotation> fieldSpecifiers = new ArrayList<Annotation>();
		List<Annotation[]> fieldEnvironments = new ArrayList<Annotation[]>();
		{
			for (Field field : getAllFields(clazz))
			{
				Annotation[] fa = field.getAnnotations();
				Annotation specifier = findSpecifier(fa);
				
				if (specifier != null)
				{
					String setter = null;
					{
						//Calculate setter name
						char first = field.getName().charAt(0);
						setter = "set" + Character.toUpperCase(first) + field.getName().substring(1);
					}
					
					
					fieldSpecifiers.add(specifier);
					fieldEnvironments.add(fa);
					fieldBindingSetterNames.add(setter);
				}
			}
		}
		
		
		//Make a single pass through the methods, then work from there
		{
			for (Method method : getAllMethods(clazz))
			{
				//Is it the defaultHandler?
				{
					if (defaultEventHandler == null)
						if (method.isAnnotationPresent(DefaultHandler.class))
							defaultEventHandler = method;
				}
				
				
				//Is it an event handler?
				{
					if (method.isAnnotationPresent(HandlesEvent.class))
						eventHandlers.add(method);
				}
				
				
				//Is it an error handler?
				{
					if (method.isAnnotationPresent(HandlesBindingError.class))
						errorHandlers.add(method);
				}
				
				
				
				//Is it part of a binding?
				{
					if (method.getParameterTypes().length > 0)
					{
						//Is it a bound field's setter?
						int index = fieldBindingSetterNames.indexOf(method.getName());
						if (index != -1)
						{
							Annotation specifier = fieldSpecifiers.get(index);
							Annotation[] environment = fieldEnvironments.get(index);
							
							//It is part of a Binding!
							bindings.add(new Binding(method, specifier, environment));
						}
						else
						{
							//not a setter
							
							Annotation[] ma = method.getAnnotations();
							
							//Is it bound itself?
							Annotation specifier = findSpecifier(ma);
							
							if (specifier != null)
							{
								//It is part of a Binding!
								bindings.add(new Binding(method, specifier, ma));
							}
						}
					}
				}
			}
		}
		
		
		//Parse out error handlers into the hashmap and nonspec array
		HashMap<String, List<Method>> nameSpecErrorHandlers = new HashMap<String, List<Method>>();
		List<Method> nameNonspecErrorHandlers = new ArrayList<Method>();
		{
			for (int i = 0; i < errorHandlers.size(); i++)
			{
				Method handler = errorHandlers.get(i);
				HandlesBindingError handles = handler.getAnnotation(HandlesBindingError.class);
				
				boolean nonspec = false;
				for (String name : handles.name())
				{
					if (name.length() == 0)
					{
						if (!nonspec)
						{
							nameNonspecErrorHandlers.add(handler);
							nonspec = true;
						}
					}
					else
					{
						List<Method> handlers = null;
						{
							handlers = nameSpecErrorHandlers.get(name);
							if (handlers == null)
							{
								handlers = new ArrayList<Method>();
								nameSpecErrorHandlers.put(name, handlers);
							}
						}
						
						handlers.add(handler);
					}
				}
			}
		}
		
		
		
		
		//Put it all together
		CachedClass cache = null;
		{
			//Transliterate namedSpecErrorHandlers
			HashMap<String, Method[]> arrayNameSpecHandlers = null;
			{
				arrayNameSpecHandlers = new HashMap<String, Method[]>();
				
				for (String name : nameSpecErrorHandlers.keySet())
				{
					List<Method> vector = nameSpecErrorHandlers.get(name);
					Method[] array = vector.toArray(new Method[vector.size()]);
					arrayNameSpecHandlers.put(name, array);
				}
			}
			
			
			cache = new CachedClass();
			cache.defaultEventHandler = defaultEventHandler;
			cache.bindings = bindings.toArray(new Binding[bindings.size()]);
			cache.eventHandlers = eventHandlers.toArray(new Method[eventHandlers.size()]);
			cache.errorHandlers = errorHandlers.toArray(new Method[errorHandlers.size()]);
			cache.nameNonSpecErrorHandlers = nameNonspecErrorHandlers.toArray(new Method[nameNonspecErrorHandlers.size()]);
			cache.nameSpecErrorHandlers = arrayNameSpecHandlers;
		}
		
		
		//Time? *pant* *pant*
		//logBenchmark(start, "Making the cache for "+clazz);
		
		return cache;
	}
	
	
	
	@Nullable
	protected static Annotation findSpecifier(Annotation[] annotations)
	{
		Annotation it = null;
		
		for (Annotation a : annotations)
		{
			requireNonNull(a);
			
			if (TypeConverterManager.isSpecifier(a))
			{
				if (it != null)
				{
					throw new IllegalStateException("Can't have more than one specifier!!: "+repr(annotations));
				}
				else
				{
					it = a;
				}
			}
		}
		
		return it;
	}
	
	//Todo what in the name of premature optimization was this ever supposed to do (namely when the specifier index != 0)   o,O!?
	//	private static Annotation[] getEnvironment(Annotation[] annotations)
	//	{
	//		Annotation[] env = new Annotation[annotations.length];
	//		int ceiling = 0;
	//		for (int i = 0; i < annotations.length; i++)
	//		{
	//			Annotation a = annotations[i];
	//			
	//			if (TypeConverterManager.isSpecifier(a))
	//			{
	//				//Reflectively pull index
	//				int index = TypeConverterManager.getSpecifierIndex(a);
	//				if (index < 0)
	//					throw new ArrayIndexOutOfBoundsException("Type converter specifier index field can't be negative!");
	//				if (index >= env.length)
	//					throw new ArrayIndexOutOfBoundsException("Type converter specifier index field can't be higher than the number of annotations!");
	//				
	//				env[index] = a;
	//				ceiling = Math.max(ceiling, index);
	//			}
	//		}
	//		
	//		//Pack env if you can
	//		{
	//			if (ceiling < env.length-1)
	//			{
	//				Annotation[] packed = new Annotation[ceiling+1];
	//				System.arraycopy(env, 0, packed, 0, packed.length);
	//				env = packed;
	//			}
	//		}
	//		
	//		return env;
	//	}
	
	
	
	protected CachedClass getCache()
	{
		return getCacheFor(getClass());
	}
	
	@ImplementationTransparency
	public static CachedClass getCacheFor(Class c)
	{
		CachedClass cache = null;
		{
			synchronized (map)
			{
				cache = map.get(c);
				
				if (cache == null)
				{
					cache = makeCacheFor(c);
					map.put(c, cache);
				}
			}
		}
		
		return cache;
	}
	//	Cache>
	//	Introspection>
}
