/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.spots.traditionaljee.util.fileupload;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO SET LAST MODIFIED!!

/**
 * Represents a single file in the value of a "file" type HTML Form Field (because the user could have selected multiple files).<br>
 * @author Sean
 */
public class FileValue<D>
{
	protected @Nullable String filename, contentType;
	protected @Nonnull D filehandle;
	protected @Nullable Long lastModified;  //like System.currentTimeMillis()
	
	
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

	public long getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}
}
