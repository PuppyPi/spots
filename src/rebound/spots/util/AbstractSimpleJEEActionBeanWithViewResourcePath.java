package rebound.spots.util;

public abstract class AbstractSimpleJEEActionBeanWithViewResourcePath
extends AbstractActionBean
implements ActionBeanWithViewResourcePath, DefaultSimpleJEEActionBeanWithViewResourcePath
{
	protected String viewResourcePath;
	
	
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
