package rebound.spots.util.binding.annotated.util;

import java.io.IOException;
import javax.servlet.ServletException;
import rebound.spots.util.ActionBeanWithViewResourcePath;
import rebound.spots.util.DefaultSimpleJEEActionBean;
import rebound.spots.util.binding.annotated.BindingAnnotatedActionBean;

public abstract class AbstractBindingAnnotatedSimpleJEEActionBeanWithViewResourcePath
extends BindingAnnotatedActionBean
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
