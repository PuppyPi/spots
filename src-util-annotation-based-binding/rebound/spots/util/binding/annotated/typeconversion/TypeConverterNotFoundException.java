/*
 * Created on Jun 2, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util.binding.annotated.typeconversion;


/**
 * The {@link TypeConverterManager} had trouble instantiating an instance.<br>
 * Check your logs.<br>
 * @author RProgrammer
 */
public class TypeConverterNotFoundException
extends Exception
{
	private static final long serialVersionUID = 1L;
	
	protected Class missingClass;
	
	public TypeConverterNotFoundException()
	{
		super();
	}
	
	public TypeConverterNotFoundException(Class missingClass)
	{
		super();
		this.missingClass = missingClass;
	}
	
	
	public Class getMissingClass()
	{
		return this.missingClass;
	}
}
