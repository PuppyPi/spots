/*
 * Created on Oct 4, 2007
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.annotated.typeconversion;

//Note: This was part of the Chained system of TypeConverters
//todo Implement this in the filter TCs
public interface FilterTypeConverter
{
	public boolean isInputSupported(Class c);
}
