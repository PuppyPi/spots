/*
 * Created on Nov 25, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.traditionaljee.multipart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.server.fileupload.AcceptFilter;
import rebound.server.fileupload.Datastore;
import rebound.server.fileupload.FileValue;
import rebound.server.multipart.impl.MultipartHandlingCore;
import rebound.spots.traditionaljee.util.AbstractHTTPFilter;
import rebound.spots.traditionaljee.util.MutableParameterServletRequest;
import rebound.util.Either;

/**
 * Yes. Another multipart resolving filter. Get over it.<br>
 * The special thing about this one is you provide your own datastore facility.<br>
 * Another noteworthy facility of this class is that it will terminate requests which provide unexpected files.<br>
 * This is good because it reduces the sting of potential DoS attacks, although it definitely does not prevent them.<br>
 * <br>
 * The handles (as returned by the {@link Datastore}) are kept in request attributes (because the normal Parameters can only hold Strings).<br>
 * Each file-type form field is stored in an attribute with the name <code>"filesvalue_<i>parameterName</i>"</code> and a value of type <code>List&lt;{@link FileValue}&gt;</code>.<br>
 * Where <code><i>parameterName</i></code> is the name of the form parameter the file was sent under.<br>
 * @see AcceptFilter
 * @see Datastore
 * @author RProgrammer
 */
public abstract class MultipartFilter<D>
extends AbstractHTTPFilter
{
	public static final String STORAGE_ATTRIBUTE_PREFIX = "filesvalue_";
	//	public static final String ACCEPT_FILTER_ATTRIBUTE = "accept_files";
	//	public static final String DATASTORE_ATTRIBUTE = "datastore";
	
	
	
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		MutableParameterServletRequest mutreq = null;
		
		//Only intercept the request if it's multipart, and thus unintelligible to the servlet container
		if (MultipartHandlingCore.isRequestMultipart(request))
		{
			AcceptFilter acceptFilter = getAcceptFilter(request, response);
			Datastore<D> datastore = getDatastore(request, response);
			
			
			Map<String, List<Either<String, FileValue<D>>>> r = MultipartHandlingCore.handle(request, acceptFilter, datastore, () -> this.kill400(request, response));
			
			if (r == null)  //error
				return;
			else
			{
				for (Entry<String, List<Either<String, FileValue<D>>>> e : r.entrySet())
				{
					String formFieldName = e.getKey();
					
					for (Either<String, FileValue<D>> formParameterValue : e.getValue())
					{
						if (formParameterValue.isA())
						{
							//Store {name,value} in request.parameters
							{
								if (mutreq == null)
								{
									//Acquire a parameter-mutable request
									if (request instanceof MutableParameterServletRequest)
										mutreq = (MutableParameterServletRequest)request;
									else
										mutreq = new SimpleMutableParameterServletRequestWrapper(request);
								}
								
								mutreq.putParameter(formFieldName, formParameterValue.getValueIfA());
							}
						}
						else
						{
							final List<FileValue<D>> filesvalue;
							{
								String name = STORAGE_ATTRIBUTE_PREFIX+formFieldName;
								Object o = request.getAttribute(name);
								
								if (o instanceof List)
									filesvalue = (List<FileValue<D>>)o;
								else
								{
									filesvalue = new ArrayList<FileValue<D>>();
									request.setAttribute(name, filesvalue);
								}
							}
							
							filesvalue.add(formParameterValue.getValueIfB());
						}
					}
				}
			}
		}
		else
		{
			//Skip non-multipart requests
		}
		
		
		chain.doFilter(mutreq != null ? mutreq : request, response);
	}
	
	private void kill400(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		request.getInputStream().close();
		response.sendError(400);
		response.flushBuffer();
	}
	
	
	
	
	
	
	
	
	
	public abstract AcceptFilter getAcceptFilter(HttpServletRequest request, HttpServletResponse response);
	
	public abstract Datastore<D> getDatastore(HttpServletRequest request, HttpServletResponse response);
}
