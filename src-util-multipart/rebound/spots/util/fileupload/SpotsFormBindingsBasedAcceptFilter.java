package rebound.spots.util.fileupload;

import static java.util.Objects.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.servlet.ServletRequest;
import rebound.server.fileupload.AcceptFilter;
import rebound.spots.util.binding.annotated.typeconversion.typeconverters.scalar.FormBoundFiles;
import rebound.util.AngryReflectionUtility;

public class SpotsFormBindingsBasedAcceptFilter
implements AcceptFilter
{
	protected final @Nonnull List<FormBoundFiles> bindings;
	
	public SpotsFormBindingsBasedAcceptFilter(@Nonnull List<FormBoundFiles> bindings)
	{
		this.bindings = requireNonNull(bindings);
	}
	
	public SpotsFormBindingsBasedAcceptFilter(@Nonnull Class actionBeanClass)
	{
		this(getFileBindings(actionBeanClass));
	}
	
	
	public boolean accept(ServletRequest req, String formName, boolean single, String filename, String contentType)
	{
		for (FormBoundFiles b : bindings)
		{
			if (b.value().equals(formName))
			{
				if (b.singletonOnly() && !single)
					return false;
				
				//Filename and contentType don't affect acceptibility
				return true;
			}
		}
		
		//No bindings for form field
		return false;
	}
	
	
	
	
	
	public static List<FormBoundFiles> getFileBindings(Class actionBeanClass)
	{
		Field[] fields = AngryReflectionUtility.getAllFields(actionBeanClass);
		
		List<FormBoundFiles> bindings = new ArrayList<>();
		{
			for (Field f : fields)
			{
				if (f.isAnnotationPresent(FormBoundFiles.class))
				{
					bindings.add(f.getAnnotation(FormBoundFiles.class));
				}
			}
		}
		
		return trimmed(bindings);
	}
}
