package rebound.spots.util.binding.annotated.util;

import rebound.spots.util.ActionBeanWithViewResourcePath;
import rebound.spots.util.DefaultSimpleJEEActionBeanWithViewResourcePath;
import rebound.spots.util.binding.annotated.BindingAnnotatedActionBean;

public abstract class AbstractBindingAnnotatedSimpleJEEActionBeanWithViewResourcePath
extends BindingAnnotatedActionBean
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
