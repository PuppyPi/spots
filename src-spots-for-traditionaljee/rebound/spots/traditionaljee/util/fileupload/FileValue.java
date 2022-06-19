/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.traditionaljee.util.fileupload;

/**
 * Represents a single file in the value of a "file" type HTML Form Field (because the user could have selected multiple files).<br>
 * @author Sean
 */
public class FileValue<D>
{
	protected String filename, contentType;
	protected D filehandle;
	
	
	public String getFilename()
	{
		return this.filename;
	}
	
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public String getContentType()
	{
		return this.contentType;
	}
	
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public D getFilehandle()
	{
		return this.filehandle;
	}

	public void setFilehandle(D filehandle)
	{
		this.filehandle = filehandle;
	}
}
