package rebound.spots.util;

import static rebound.text.StringUtilities.*;
import java.io.IOException;
import javax.servlet.ServletException;
import rebound.annotations.hints.ImplementationTransparency;

public interface DefaultSimpleJEEActionBeanWithViewResourcePath
extends ActionBeanWithViewResourcePath, DefaultSimpleJEEActionBean
{
	/**
	 * Delegates to {@link #serveJSPView()} ifF the {@link #getViewResourcePath()} ends in ".jsp" (case-insensitively)
	 * Otherwise delegates to {@link #serveStaticView()}.
	 */
	@ImplementationTransparency  //Java doesn't allow non-public methods in interfaces and doesn't allow multiple-inheritance in non-interfaces X'D
	public default void serveView() throws ServletException, IOException
	{
		if (endsWithCaseInsensitively(getViewResourcePath(), ".jsp"))
			serveJSPView();
		else
			serveStaticView();
	}
	
	
	
	/**
	 * @see #serveStaticView(String)
	 */
	@ImplementationTransparency  //Java doesn't allow non-public methods in interfaces and doesn't allow multiple-inheritance in non-interfaces X'D
	public default void serveStaticView() throws ServletException, IOException
	{
		serveStaticView(getViewResourcePath());
	}
	
	/**
	 * @see #serveJSPView(String)
	 */
	@ImplementationTransparency  //Java doesn't allow non-public methods in interfaces and doesn't allow multiple-inheritance in non-interfaces X'D
	public default void serveJSPView() throws ServletException, IOException
	{
		serveJSPView(getViewResourcePath());
	}
}
