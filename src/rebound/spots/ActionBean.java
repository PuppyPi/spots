/*
 * Created on May 23, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots;

import java.io.IOException;
import javax.servlet.ServletException;

public interface ActionBean
{
	public void doAction() throws ServletException, IOException;
	
	public ActionBeanContext getContext();
	
	public void setContext(ActionBeanContext context);
}
