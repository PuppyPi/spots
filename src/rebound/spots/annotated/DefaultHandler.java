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
 * @author RProgrammer
 * Signifies that the target method handles all events which do not fall under the jurisdiction of any other {@link HandlesEvent event handlers}.<br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DefaultHandler
{
	
}
