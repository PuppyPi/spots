/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.annotations.hints.IntendedToNOTBeSubclassedImplementedOrOverriddenByApiUser;

public interface ActionBean
{
	public void doAction() throws ServletException, IOException;
	
	public ActionBeanContext getContext();
	
	public void setContext(ActionBeanContext context);
	
	
	
	public default HttpServletRequest getRequest()
	{
		return getContext().getRequest();
	}
	
	public default HttpServletResponse getResponse()
	{
		return getContext().getResponse();
	}
}
