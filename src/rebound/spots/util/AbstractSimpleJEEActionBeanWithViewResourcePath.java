package rebound.spots.util;

import java.io.IOException;
import javax.servlet.ServletException;

public abstract class AbstractSimpleJEEActionBeanWithViewResourcePath
extends AbstractActionBean
implements ActionBeanWithViewResourcePath, DefaultSimpleJEEActionBean
{
	protected String viewResourcePath;
	
	
	/**
	 * @see #serveStaticView(String)
	 */
	protected void serveStaticView() throws ServletException, IOException
	{
		serveStaticView(getViewResourcePath());
	}
	
	/**
	 * @see #serveJSPView(String)
	 */
	protected void serveJSPView() throws ServletException, IOException
	{
		serveJSPView(getViewResourcePath());
	}
	
	
	
	
	@Override
	public void setViewResourcePath(String path)
	{
		this.viewResourcePath = path;
	}
	
	@Override
	public String getViewResourcePath()
	{
		return viewResourcePath;
	}
}
