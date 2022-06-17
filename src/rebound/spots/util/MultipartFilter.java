/*
 * Created on Nov 25, 2007
 * 	by the great Eclipse(c)
 */
package rebound.spots.util;

import java.awt.datatransfer.MimeTypeParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.io.util.TextIOUtilities;
import rebound.spots.util.apacheupload.MultipartStream;
import rebound.util.MIMEHeaders;
import rebound.util.MIMEHeaders.Header;
import rebound.util.MimeTypeParameterList;

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
public abstract class MultipartFilter
extends AbstractHTTPFilter
{
	public static final String STORAGE_ATTRIBUTE_PREFIX = "filesvalue_";
//	public static final String ACCEPT_FILTER_ATTRIBUTE = "accept_files";
//	public static final String DATASTORE_ATTRIBUTE = "datastore";
	
	
	
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		MutableParameterServletRequest mutreq = null;
		
		//Only intercept the request if it's multipart, and thus unintelligible to the servlet container
		if (request.getContentType() != null && request.getContentType().startsWith("multipart"))
		{
			AcceptFilter acceptFilter = getAcceptFilter(request, response);
			Datastore datastore = getDatastore(request, response);
			
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
							kill400(request, response);
							return;
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
					kill400(request, response);
					return;
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
						kill400(request, response);
						return;
					}
				}
				
				
				Header contentDispositionHeader = headers.getHeader("Content-Disposition");
				
				if (contentDispositionHeader == null)
				{
					kill400(request, response);
					return;
				}
				else if (contentDispositionHeader.getValue().equals("form-data"))
				{
					String formFieldName = null;
					{
						formFieldName = contentDispositionHeader.getParameterValue("name");
						if (formFieldName == null || formFieldName.length() == 0)
						{
							kill400(request, response);
							return;
						}
					}
					
					if (formFieldName != null && formFieldName.length() > 0)
					{
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
									if (mutreq == null)
									{
										//Acquire a parameter-mutable request
										if (request instanceof MutableParameterServletRequest)
											mutreq = (MutableParameterServletRequest)request;
										else
											mutreq = new SimpleMutableParameterServletRequestWrapper(request);
									}
									
									mutreq.putParameter(formFieldName, value);
								}
							}
						}
						else
						{
							//File form data
							{
								List<FileValue> filesvalue = null;
								{
									String name = STORAGE_ATTRIBUTE_PREFIX+formFieldName;
									Object o = request.getAttribute(name);
									if (o instanceof List)
										filesvalue = (List<FileValue>)o;
									if (filesvalue == null)
									{
										filesvalue = new ArrayList<FileValue>();
										request.setAttribute(name, filesvalue);
									}
								}
								
								//If this file will be the second+ file for the given form field
								boolean multipleFileValues = !filesvalue.isEmpty() || mixed;
								
								//Make sure it's not rejected (for mixed, this is just a once-over check of all files at once, because they share a lot of attributes)
								if (!checkAccept(acceptFilter, request, response, formFieldName, !multipleFileValues, headers))
									return;
								
								
								
								//Download the data
								
								if (!mixed)
								{
									String filename = contentDispositionHeader.getParameterValue("filename");
									
									
									//Skip empty file boxes
									if (filename != null && filename.length() > 0)
									{
										//Single file
										InputStream dataIn = multipartStream.newInputStream();
										
										Object filehandle = datastore.store(dataIn, -1, filename, contentType);
										
										if (filehandle == null)
										{
											dataIn.close();
											multipartStream.discardBodyData();
										}
										
										FileValue value = new FileValue();
										value.setContentType(contentType);
										value.setFilename(filename);
										value.setFilehandle(filehandle);
										
										filesvalue.add(value);
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
											kill400(request, response);
											return;
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
												kill400(request, response);
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
											if (!checkAccept(acceptFilter, request, response, formFieldName, false, currFileHeaders))
												return;
											
											String currContentType = currFileHeaders.getHeaderValue("Content-Type");
											
											InputStream currDataIn = multipartStream.newInputStream();
											
											Object currFilehandle = datastore.store(currDataIn, -1, currFilename, currContentType);
											
											if (currFilehandle == null)
											{
												currDataIn.close();
												multipartStream.discardBodyData();
											}
											
											
											FileValue currValue = new FileValue();
											currValue.setContentType(currContentType);
											currValue.setFilename(currFilename);
											currValue.setFilehandle(currFilehandle);
											
											filesvalue.add(currValue);
										}
										
										filesContinewe = filesMultipartStream.readBoundary();
									}
									
									((ArrayList)filesvalue).trimToSize();
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
	
	private boolean checkAccept(AcceptFilter acceptFilter, HttpServletRequest request, HttpServletResponse response, String formFieldName, boolean singletonFile, MIMEHeaders headers) throws IOException
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
			response.getOutputStream().close();
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
	
	
	
	
	
	
	
	
	
	public abstract AcceptFilter getAcceptFilter(HttpServletRequest request, HttpServletResponse response);
	
	public abstract Datastore getDatastore(HttpServletRequest request, HttpServletResponse response);
}
