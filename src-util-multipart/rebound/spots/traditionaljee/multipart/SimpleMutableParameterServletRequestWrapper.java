/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.traditionaljee.multipart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import rebound.spots.traditionaljee.util.MutableParameterServletRequest;

/**
 * This class should never be used (IT DOESN'T IMPLEMENT {@link HttpServletRequest HTTPSERVLETREQUEST}!!!), but is provided as a backup in case no filter wraps the request in a subclass of {@link MutableParameterServletRequest} before it reaches the {@link MultipartFilter}.<br>
 * @author Sean
 */
public class SimpleMutableParameterServletRequestWrapper
extends ServletRequestWrapper
implements MutableParameterServletRequest
{
	protected Map<String, String> freshParameters;
	
	public SimpleMutableParameterServletRequestWrapper(ServletRequest request)
	{
		super(request);
		freshParameters = new HashMap<String, String>();
	}
	
	
	
	//<Parameter subsystem
	public void putParameter(String key, String value)
	{
		freshParameters.put(key, value);
	}
	
	
	@Override
	public String getParameter(String name)
	{
		String v = freshParameters.get(name);
		if (v == null)
			v = getRequest().getParameter(name);
		return v;
	}
	
	@Override
	public Enumeration<String> getParameterNames()
	{
		Enumeration<String> oldnames = getRequest().getParameterNames();
		if (freshParameters.isEmpty())
		{
			return oldnames;
		}
		else if (oldnames == null)
		{
			return Collections.enumeration(freshParameters.keySet());
		}
		else
		{
			Set<String> freshnames = freshParameters.keySet();
			
			ArrayList<String> newnames = new ArrayList<String>();
			newnames.addAll(freshnames);
			while (oldnames.hasMoreElements())
				newnames.add(oldnames.nextElement());
			removeDuplicates(newnames);
			
			return Collections.enumeration(newnames);
		}
	}
	
	@Override
	public String[] getParameterValues(String name)
	{
		String[] oldvals = getRequest().getParameterValues(name);
		String freshval = freshParameters.get(name);
		
		if (freshval == null)
		{
			return oldvals;
		}
		else if (oldvals == null || oldvals.length == 0)
		{
			return new String[]{freshval};
		}
		else
		{
			String[] newvals = new String[oldvals.length+1];
			System.arraycopy(oldvals, 0, newvals, 0, oldvals.length);
			newvals[newvals.length-1] = freshval;
			return newvals;
		}
	}
	
	@Override
	public Map<String, String[]> getParameterMap()
	{
		Map oldmap = getRequest().getParameterMap();
		
		if (freshParameters.isEmpty())
		{
			return oldmap;
		}
		else
		{
			HashMap<String, String[]> newmap = new HashMap<String, String[]>();
			if (oldmap != null)
				newmap.putAll(oldmap);
			for (String k : freshParameters.keySet())
			{
				String[] vs = newmap.get(k);
				if (vs == null || vs.length == 0)
				{
					newmap.put(k, new String[]{freshParameters.get(k)});
				}
				else
				{
					String[] nvs = new String[vs.length+1];
					System.arraycopy(vs, 0, nvs, 0, vs.length);
					nvs[nvs.length-1] = freshParameters.get(k);
					newmap.put(k, nvs);
				}
			}
			return newmap;
		}
	}
	
	private static void removeDuplicates(ArrayList<String> list)
	{
		boolean duplicates = false;
		for (int indexA = 0; indexA < list.size()-1;)
		{
			for (int indexB = indexA+1; indexB < list.size(); indexB++)
			{
				//Calculate $duplicates
				{
					String a = list.get(indexA);
					String b = list.get(indexB);
					
					if (a == null)
						duplicates = b == null;
					else
						duplicates = b.equals(a);
				}
				
				if (duplicates)
				{
					list.remove(indexA);
					break;
				}
			}
			
			if (!duplicates)
				indexA++;
		}
	}
	//Parameter subsystem>
}
