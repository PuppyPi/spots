/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ActionBean
{
	public void doAction() throws ServletException, IOException;
	
	public ActionBeanContext getActionBeanContext();
	
	public void setActionBeanContext(ActionBeanContext context);
	
	
	
	public default HttpServletRequest getRequest()
	{
		return getActionBeanContext().getRequest();
	}
	
	public default HttpServletResponse getResponse()
	{
		return getActionBeanContext().getResponse();
	}
}
