/*
 * Created on May 28, 2007
 * 	by the great Eclipse(c)
 */
package rebound.simplejee;

import static rebound.util.collections.CollectionUtilities.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import rebound.annotations.semantic.allowedoperations.WritableValue;

public class HttpServletRequestWithNewParametersDecorator
extends HttpServletRequestWrapper
{
	protected Map<String, List<String>> newParameters;
	
	
	public HttpServletRequestWithNewParametersDecorator(HttpServletRequest underlying, Map<String, List<String>> newParameters)
	{
		super(underlying);
	}
	
	
	public static HttpServletRequest decorateWithExtraParameters(HttpServletRequest underlying, Map<String, List<String>> extraParameters)
	{
		if (extraParameters.isEmpty())
			return underlying;
		else
		{
			//Todo consider a lazy unioning Map implementation with a Map view-wrapper of the underlying HttpServletRequest here!   (Instead of a whole other implementation of this class!)  :>
			
			@WritableValue Map<String, List<String>> newParameters = null;
			
			for (String name : singleUseIterable(underlying.getParameterNames()))
			{
				if (newParameters == null)
					newParameters = new HashMap<>();
				
				newParameters.put(name, asList(underlying.getParameterValues(name)));
			}
			
			Map<String, List<String>> finalParameters;
			{
				//Todo consider a lazy unioning Map implementation with the above map taken as immutable, here!   (Instead of a whole other implementation of this class!)  :>
				
				if (newParameters == null)
					finalParameters = extraParameters;  //if there were no old parameters, just use the extras directly! X3
				else
				{
					for (Entry<String, List<String>> e : extraParameters.entrySet())
					{
						String name = e.getKey();
						List<String> current = newParameters.get(e);
						List<String> extra = e.getValue();
						
						List<String> combined = current == null ? extra : concatenateListsOPC(current, extra);
						
						newParameters.put(name, combined);
					}
					
					finalParameters = newParameters;
				}
			}
			
			return new HttpServletRequestWithNewParametersDecorator(underlying, finalParameters);
		}
	}
	
	
	
	
	
	
	
	
	@Override
	public String getParameter(String name)
	{
		List<String> l = newParameters.get(name);
		return (l == null || l.isEmpty()) ? null : l.get(0);
	}
	
	@Override
	public Map<String, String[]> getParameterMap()
	{
		return mapdictvalues(l -> l.toArray(new String[0]), newParameters);
	}
	
	public Map<String, List<String>> getParameterMap2()
	{
		return newParameters;
	}
	
	@Override
	public Enumeration<String> getParameterNames()
	{
		return Collections.enumeration(newParameters.keySet());
	}
	
	@Override
	public String[] getParameterValues(String name)
	{
		return super.getParameterValues(name);
	}
}
