package rebound.spots;

public interface ExtraActionBeanContext
extends ActionBeanContext
{
	/**
	 * Gets the names of all parameters specific to this type of action bean (they are not specific to this instance, though).<br>
	 */
	public String[] getInitParameterNames();
	
	/**
	 * Gets a parameter specific to this type of action bean (they are not specific to this instance, though) or <code>null</code> if one could not be found.<br>
	 */
	public String getInitParameter(String name);
	
	/**
	 * Gets the names of all parameters found by the dispatcher.<br>
	 */
	public String[] getGlobalInitParameterNames();
	
	/**
	 * Gets a parameter found by the dispatcher or <code>null</code> if one could not be found.<br>
	 */
	public String getGlobalInitParameter(String name);
	
	
	/**
	 * This gets a {@link #getInitParameter(String) actionbean class - specific} parameter.<br>
	 * But if one cannot be found, returns a {@link #getGlobalInitParameter(String) global init parameter}.<br>
	 * If <i>that</i> cannot be found, returns <code>null</code>.<br>
	 */
	public String getAParameter(String name);
}
