package rebound.simplejee.fileupload;

import static rebound.util.collections.CollectionUtilities.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import rebound.server.fileupload.AcceptFilter;
import rebound.server.fileupload.Datastore;
import rebound.server.fileupload.FileValue;
import rebound.server.multipart.impl.MultipartHandlingCore;
import rebound.simplejee.HttpServletRequestWithNewParametersDecorator;
import rebound.spots.traditionaljee.multipart.MultipartFilter;
import rebound.util.Either;
import rebound.util.collections.FilterAwayReturnPath;

public class SimpleJEEMultipartHandling
{
	/**
	 * @return the {@link HttpServletRequest} to use which has the new form parameters (or possibly the input one provided, passed through), or null if and only if HTTP 400 error should be sent to the client!
	 */
	public static <D> HttpServletRequest filter(HttpServletRequest request, AcceptFilter acceptFilter, Datastore<D> datastore) throws IOException
	{
		if (MultipartHandlingCore.isRequestMultipart(request))  //this is the pathway like .1% of invocations take XD
		{
			Map<String, List<Either<String, FileValue<D>>>> r = MultipartHandlingCore.handle(request, acceptFilter, datastore);
			
			if (r == null)
				return null;
			else if (r.isEmpty())
				return request;
			else
			{
				Map<String, List<String>> textParameters;
				//Map<String, List<FileValue<D>>> fileParameters;
				{
					textParameters = null;
					//fileParameters = null;
					
					for (Entry<String, List<Either<String, FileValue<D>>>> e : r.entrySet())
					{
						String name = e.getKey();
						List<Either<String, FileValue<D>>> vs = e.getValue();
						
						
						List<String> textValues = mapToList(v ->
						{
							if (v.isA())
								return v.getValueIfA();
							else
								throw FilterAwayReturnPath.I;
						}, vs);
						
						
						List<FileValue<D>> fileValues = mapToList(v ->
						{
							if (v.isB())
								return v.getValueIfB();
							else
								throw FilterAwayReturnPath.I;
						}, vs);
						
						
						if (!textValues.isEmpty())
						{
							if (textParameters == null)
								textParameters = new HashMap<>();
							putNewMandatory(textParameters, name, textValues);
						}
						
						
						if (!fileValues.isEmpty())
						{
							//	if (fileParameters == null)
							//		fileParameters = new HashMap<>();
							//	putNewMandatory(fileParameters, name, fileValues);
							
							request.setAttribute(MultipartFilter.STORAGE_ATTRIBUTE_PREFIX+name, fileValues);  //TODO update how type converters work so we don't have to use unsemantic attributessssss! X'D
						}
					}
				}
				
				
				return textParameters == null ? request : HttpServletRequestWithNewParametersDecorator.decorateWithExtraParameters(request, textParameters);
			}
		}
		
		return request;
	}
}
