package rebound.spots.util;

public abstract class AbstractSimpleJEEActionBeanWithViewResourcePath
extends AbstractActionBean
implements ActionBeanWithViewResourcePath, DefaultSimpleJEEActionBeanWithViewResourcePath
{
	protected String viewResourcePath;
	
	protected void setViewResourcePath(String path)
	{
		this.viewResourcePath = path;
	}
	
	@Override
	public String getViewResourcePath()
	{
		return viewResourcePath;
	}
}
