/*
 * Created on Jun 3, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.annotated;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates which events the target method handles the logic for.<br>
 * Events are determined by their existence as a form parameter (because submit buttons' values are inseparable from their descriptive labels).<br>
 * For the target method to be invoked, at least one of the form parameters defined in this annotation must exist in the form.<br>
 * @author RProgrammer
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandlesEvent
{
	String[] value();
}
