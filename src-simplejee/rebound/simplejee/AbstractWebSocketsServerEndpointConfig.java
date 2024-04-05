package rebound.simplejee;

import static java.util.Collections.*;
import java.util.List;
import java.util.Map;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public abstract class AbstractWebSocketsServerEndpointConfig<E extends Endpoint>
implements ServerEndpointConfig
{
	public abstract E newConnectionReceived();
	
	@Override
	public abstract Class<E> getEndpointClass();
	
	
	/**
	 * @param originHeaderValue "http://example.com" for example!  You need to make sure it's from your website otherwise the web browser could be faithfully telling you that the user it's the agent of is being duped by malicious JavaScript somewhere else on the internet!  (Obviously if the user is malicious they can just set the Origin parameter to whatever they want though X3 so this is for keeping the actual trusted users safe from JavaScript on other websites doing Funny Things!)
	 */
	public abstract boolean checkOrigin(String originHeaderValue);
	
	
	
	
	
	protected Configurator configurator = new Configurator()
	{
		@Override
		public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException
		{
			return (T)newConnectionReceived();
		}
		
		@Override
		public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested)
		{
			return emptyList();
		}
		
		@Override
		public String getNegotiatedSubprotocol(List<String> supported, List<String> requested)
		{
			return "";
		}
		
		@Override
		public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response)
		{
		}
		
		@Override
		public boolean checkOrigin(String originHeaderValue)
		{
			return super.checkOrigin(originHeaderValue);
		}
	};
	
	@Override
	public Configurator getConfigurator()
	{
		return configurator;
	}
	
	
	
	
	@Override
	public List<Class<? extends Encoder>> getEncoders()
	{
		return emptyList();
	}
	
	@Override
	public List<Class<? extends Decoder>> getDecoders()
	{
		return emptyList();
	}
	
	@Override
	public Map<String, Object> getUserProperties()
	{
		return emptyMap();
	}
	
	@Override
	public List<String> getSubprotocols()
	{
		return emptyList();
	}
	
	@Override
	public List<Extension> getExtensions()
	{
		return emptyList();
	}
}
