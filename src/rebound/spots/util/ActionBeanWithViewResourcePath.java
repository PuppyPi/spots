package rebound.spots.util;

import java.util.UUID;
import rebound.spots.ActionBean;

/**
 * Note that subclasses don't need to use JSP, this system works for be anything.  It's just for when whatever is used has a separate View system that uses pathnames to identify the pages / pagetemplates (instead of idk, {@link UUID}s? XD).
 */
public interface ActionBeanWithViewResourcePath
extends ActionBean
{
	/**
	 * @param path  eg, "/styles/main.css" or "/index.jsp" or "/forum/threads.jsp" or "/errors/notfound.jsp" or etc. :3
	 */
	public void setViewResourcePath(String path);
	
	public String getViewResourcePath();
}
