/*
 * Created on Jan 30, 2008
 * 	by the wonderful Eclipse(c)
 */
package rebound.server.fileupload;

import javax.servlet.ServletRequest;

public interface AcceptFilter
{
	/**
	 * This allows the MultipartFilter to query the application logic about whether a certain file should be allowed to be uploaded.<br>
	 * @param formName The name of the form field the files are filed under
	 * @param single If there is just one file being uploaded under the given form field (so far)
	 * @param filename If <code>single == true</code>, this is the provided filename. Otherwise, this is null.
	 * @param contentType If <code>single == true</code>, this is the provided content-type, without parameters (defaults to <code>"application/octet-stream"</code>). If single is false, this is null
	 * @return True if the file is allowed. If this is false, the connection will be terminated.
	 */
	public boolean accept(ServletRequest req, String formName, boolean single, String filename, String contentType);
}
