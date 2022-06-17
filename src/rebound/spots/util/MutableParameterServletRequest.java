/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.util;

import javax.servlet.ServletRequest;

public interface MutableParameterServletRequest
extends ServletRequest
{
	public void putParameter(String key, String value);
}
