package rebound.server.fileupload.util;

import javax.servlet.ServletRequest;
import rebound.server.fileupload.AcceptFilter;

public enum NullAcceptFilter
implements AcceptFilter
{
	I;
	
	@Override
	public boolean accept(ServletRequest req, String formName, boolean single, String filename, String contentType)
	{
		return false;
	}
}
