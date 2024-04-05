package rebound.simplejee;

import static java.util.Collections.*;
import java.util.Set;
import javax.websocket.server.ServerApplicationConfig;

/**
 * Tomcat or an analogous JavaEE server container will scan (and load and call clinit, btw!!!) all the classes and then instantiate with a public no-args constructor
 * EVERY CLASS THAT IMPLEMENTS {@link ServerApplicationConfig}!!  (In an exported module package, if it's Java 9+)
 * 
 * That's how we get located and identified!!
 */
public abstract class AbtractWebsocketServerApplicationConfig
implements ServerApplicationConfig
{
	public AbtractWebsocketServerApplicationConfig()
	{
	}
	
	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned)
	{
		return emptySet();
	}
}
