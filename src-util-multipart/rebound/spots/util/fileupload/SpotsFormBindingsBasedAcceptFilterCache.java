package rebound.spots.util.fileupload;

import static java.util.Objects.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.server.fileupload.AcceptFilter;
import rebound.server.fileupload.util.NullAcceptFilter;
import rebound.spots.util.binding.annotated.typeconversion.typeconverters.scalar.FormBoundFiles;
import rebound.util.collections.FilterAwayReturnPath;

public class SpotsFormBindingsBasedAcceptFilterCache
{
	protected Map<Class<?>, AcceptFilter> exhaustiveEagerCache;
	
	public SpotsFormBindingsBasedAcceptFilterCache(Iterable<Class<?>> exhaustiveListOfAllActionBeanClasses)
	{
		requireNonNull(exhaustiveListOfAllActionBeanClasses);
		
		exhaustiveEagerCache = maptodictSameKeys(c ->
		{
			List<FormBoundFiles> b = SpotsFormBindingsBasedAcceptFilter.getFileBindings(c);
			
			if (b.isEmpty())
				throw FilterAwayReturnPath.I;
			else
				return new SpotsFormBindingsBasedAcceptFilter(b);
			
		}, asSetUniqueifying(exhaustiveListOfAllActionBeanClasses));
	}
	
	
	public @Nullable AcceptFilter getAcceptFilterForActionBeanClassOrNull(Class<?> actionBeanClass)
	{
		return exhaustiveEagerCache.get(actionBeanClass);
	}
	
	public @Nonnull AcceptFilter getAcceptFilterForActionBeanClass(Class<?> actionBeanClass)
	{
		AcceptFilter r = getAcceptFilterForActionBeanClassOrNull(actionBeanClass);
		return r == null ? NullAcceptFilter.I : r;
	}
}
