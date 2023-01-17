package rebound.server.multipart.impl;

import static java.util.Collections.*;
import java.awt.datatransfer.MimeTypeParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import rebound.io.util.TextIOUtilities;
import rebound.server.fileupload.AcceptFilter;
import rebound.server.fileupload.Datastore;
import rebound.server.fileupload.FileValue;
import rebound.server.multipart.fromothers.MimeTypeParameterList;
import rebound.server.multipart.fromothers.MultipartStream;
import rebound.util.Either;
import rebound.util.MIMEHeaders;
import rebound.util.MIMEHeaders.Header;
import rebound.util.container.ContainerInterfaces.ObjectContainer;
import rebound.util.container.SimpleContainers.SimpleObjectContainer;
import rebound.util.functional.FunctionInterfaces.UnaryProcedure;
import rebound.util.functional.throwing.FunctionalInterfacesThrowingCheckedExceptionsStandard.RunnableThrowingIOException;

/**
 * "MIME Multipart" is a way of doing form data posting from HTTP client to server that works for anything.
 * But it is the *only* way files are uploaded!
 */
public class MultipartHandlingCore
{
	public static boolean isRequestMultipart(HttpServletRequest request)
	{
		//Only intercept the request if it's multipart, and thus unintelligible to the servlet container
		return request.getContentType() != null && request.getContentType().startsWith("multipart");
	}
	
	
	
	/**
	 * @return the Form Parameters read (not just necessarily files, also other things too!!)  Will be null if and only if kill400 was called! otherwise a list that may or may not be empty (technically a valid MIME Multipart Message doesn't have to have an actual file upload in it!)
	 */
	public static @Nullable <D> Map<String, List<Either<String, FileValue<D>>>> handle(HttpServletRequest request, AcceptFilter acceptFilter, Datastore<D> datastore, RunnableThrowingIOException kill400) throws IOException
	{
		ObjectContainer<Map<String, List<Either<String, FileValue<D>>>>> formParametersFoundC = new SimpleObjectContainer<>();
		
		byte[] boundary = null;
		{
			MimeTypeParameterList params = null;
			{
				String paramData = null;
				{
					String contentType = request.getContentType();
					
					int semicolonIndex = contentType.indexOf(';');
					if (semicolonIndex != -1)
						paramData = contentType.substring(semicolonIndex);
					else
						paramData = "";
				}
				
				if (paramData.length() > 0)
				{
					try
					{
						params = new MimeTypeParameterList(paramData);
					}
					catch (MimeTypeParseException exc)
					{
						//Syntax Error
						kill400.run();
						return null;
					}
				}
				else
				{
					params = null;
				}
			}
			
			
			String cbound = null;
			if (params != null)
				cbound = params.get("boundary");
			
			if (cbound == null || cbound.length() == 0)
			{
				//No Boundary!
				kill400.run();
				return null;
			}
			
			boundary = cbound.getBytes("ASCII");
		}
		
		
		
		MultipartStream multipartStream = new MultipartStream(request.getInputStream());
		multipartStream.setBoundary(boundary);
		
		
		boolean continewe = multipartStream.skipPreamble();
		
		while (continewe)
		{
			//Process encapsulation
			MIMEHeaders headers = null;
			{
				try
				{
					headers = MIMEHeaders.parse(multipartStream.readHeaders());
				}
				catch (MimeTypeParseException exc)
				{
					kill400.run();
					return null;
				}
			}
			
			
			Header contentDispositionHeader = headers.getHeader("Content-Disposition");
			
			if (contentDispositionHeader == null)
			{
				kill400.run();
				return null;
			}
			else if (contentDispositionHeader.getValue().equals("form-data"))
			{
				final String formFieldName;
				{
					formFieldName = contentDispositionHeader.getParameterValue("name");
					
					if (formFieldName == null || formFieldName.length() == 0)
					{
						kill400.run();
						return null;
					}
				}
				
				if (formFieldName != null && formFieldName.length() > 0)
				{
					UnaryProcedure<Either<String, FileValue<D>>> gotOne = thisOne ->
					{
						Map<String, List<Either<String, FileValue<D>>>> formParametersFound = formParametersFoundC.get();
						
						if (formParametersFound == null)
						{
							formParametersFound = new HashMap<>();
							formParametersFoundC.set(formParametersFound);
						}
						
						List<Either<String, FileValue<D>>> forThisField = formParametersFound.get(formFieldName);
						
						if (forThisField instanceof ArrayList)  //isMutable(forThisField)
							forThisField.add(thisOne);
						else
						{
							if (forThisField == null)
								forThisField = singletonList(thisOne);
							else
							{
								forThisField = new ArrayList<>();
								forThisField.add(thisOne);
							}
							
							formParametersFound.put(formFieldName, forThisField);
						}
					};
					
					
					Header contentTypeHeader = headers.getHeader("Content-Type");
					String contentType = (contentTypeHeader == null ? null : contentTypeHeader.getValue());  //Todo should this keep the mime parameters (like charset=...)?
					
					boolean mixed = "multipart/mixed".equals(contentType);
					
					if (!mixed && contentDispositionHeader.getParameterValue("filename") == null)
					{
						//Simple form data
						{
							String value = null;
							{
								String charset = null;
								{
									if (contentTypeHeader != null)
										charset = contentTypeHeader.getParameterValue("charset");
									if (charset == null)
										charset = "UTF-8";
								}
								
								value = new String(TextIOUtilities.readAll(new InputStreamReader(multipartStream.newInputStream(), charset)));
							}
							
							//Store {name,value} in request.parameters
							{
								gotOne.f(Either.forA(value));
							}
						}
					}
					else
					{
						//File form data
						{
							//If this file will be the second+ file for the given form field
							Map<String, List<Either<String, FileValue<D>>>> formParametersFound = formParametersFoundC.get();
							boolean multipleFileValues = (formParametersFound != null && formParametersFound.containsKey(formFieldName)) || mixed;
							
							//Make sure it's not rejected (for mixed, this is just a once-over check of all files at once, because they share a lot of attributes)
							if (!checkAccept(acceptFilter, request, formFieldName, !multipleFileValues, headers))
							{
								kill400.run();
								return null;
							}
							
							
							
							//Download the data
							
							if (!mixed)
							{
								String filename = contentDispositionHeader.getParameterValue("filename");
								
								
								//Skip empty file boxes
								if (filename != null && !filename.isEmpty())
								{
									//Single file
									InputStream dataIn = multipartStream.newInputStream();
									
									D filehandle = datastore.store(dataIn, -1, filename, contentType);
									
									if (filehandle == null)
									{
										dataIn.close();
										multipartStream.discardBodyData();
									}
									
									FileValue<D> value = new FileValue<>();
									value.setContentType(contentType);
									value.setFilename(filename);
									value.setFilehandle(filehandle);
									
									gotOne.f(Either.forB(value));
								}
							}
							
							
							else
							{
								//TODO Softcode a notification system for here ^^''
								//		Root.getPostmaster().notifyUnexpectedEvent("--------------------------------------------------------------------------------------------------------------------------Nested Multiparts!-------------------------------------------------------------------------------------------------------------------------- just thought you ought ta know.");
								
								//Multiple files
								byte[] filesBoundary = null;
								{
									String cbound = contentTypeHeader.getParameterValue("boundary");
									if (cbound != null)
										filesBoundary = cbound.getBytes("ASCII");
									if (filesBoundary == null)
									{
										kill400.run();
										return null;
									}
								}
								
								MultipartStream filesMultipartStream = new MultipartStream(multipartStream.newInputStream());
								filesMultipartStream.setBoundary(filesBoundary);
								
								boolean filesContinewe = filesMultipartStream.skipPreamble();
								
								while (filesContinewe)
								{
									MIMEHeaders currFileHeaders = null;
									{
										String headerData = filesMultipartStream.readHeaders();
										try
										{
											currFileHeaders = MIMEHeaders.parse(headerData);
										}
										catch (MimeTypeParseException exc)
										{
											kill400.run();
										}
									}
									
									String currFilename = null;
									{
										Header currContentDispositionHeader = currFileHeaders.getHeader("Content-Disposition");
										if (currContentDispositionHeader != null)
											currFilename = currContentDispositionHeader.getParameterValue("filename");
									}
									
									//Skip empty file boxes
									if (currFilename != null && currFilename.length() > 0)
									{
										//Make sure it's not rejected
										if (!checkAccept(acceptFilter, request, formFieldName, false, currFileHeaders))
										{
											kill400.run();
											return null;
										}
										
										String currContentType = currFileHeaders.getHeaderValue("Content-Type");
										
										InputStream currDataIn = multipartStream.newInputStream();
										
										D currFilehandle = datastore.store(currDataIn, -1, currFilename, currContentType);
										
										if (currFilehandle == null)
										{
											currDataIn.close();
											multipartStream.discardBodyData();
										}
										
										
										FileValue<D> currValue = new FileValue<>();
										currValue.setContentType(currContentType);
										currValue.setFilename(currFilename);
										currValue.setFilehandle(currFilehandle);
										
										gotOne.f(Either.forB(currValue));
									}
									
									filesContinewe = filesMultipartStream.readBoundary();
								}
							}
						}
					}
				}
			}
			else
			{
				//Ignore unsupported Content-Dispositions
			}
			
			continewe = multipartStream.readBoundary();
		}
		
		
		
		Map<String, List<Either<String, FileValue<D>>>> formParametersFound = formParametersFoundC.get();
		return formParametersFound == null ? emptyMap() : formParametersFound;
	}
	
	
	
	
	
	
	protected static boolean checkAccept(AcceptFilter acceptFilter, HttpServletRequest request, String formFieldName, boolean singletonFile, MIMEHeaders headers) throws IOException
	{
		String filename = null;
		{
			Header cd = headers.getHeader("Content-Disposition");
			if (cd != null)
				filename = cd.getParameterValue("filename");
		}
		
		boolean acc = false;
		{
			if (acceptFilter == null || filename == null)
				acc = false;
			else
				acc = acceptFilter.accept(request, formFieldName, singletonFile, filename, headers.getHeaderValue("Content-Type"));
		}
		
		if (!acc)
		{
			request.getInputStream().close();
			System.err.println("MultipartFilter) ! File upload rejected:");
			System.err.println("\tField name: \""+formFieldName+"\"");
			System.err.println("\tFilename: \""+filename+"\"");
			System.err.println("\tContent-Type: "+headers.getHeaderValue("Content-Type"));
			System.err.println("\tMultiple files on field: "+!singletonFile);
			System.err.println("\tContent-Length: "+headers.getHeaderValue("Content-Length"));
		}
		
		return acc;
	}
}
