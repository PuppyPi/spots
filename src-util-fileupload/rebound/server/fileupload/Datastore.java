/*
 * Created on Nov 25, 2007
 * 	by the great Eclipse(c)
 */
package rebound.server.fileupload;

import java.io.IOException;
import java.io.InputStream;

public interface Datastore<D>
{
	/**
	 * Store the data and create a reference to it.<br>
	 * The resulting object will be used as the value in a form parameter.<br>
	 * If the length is specified, then no bytes extra bytes should be read.<br>
	 * If the length is not specified (negative value), then the entire stream should be consumed until EOF is encountered.<br>
	 * @param data An InputStream which will give -1 to indicate EOF, as per the java.io spec
	 * @param length The number of bytes to read from the stream, if unknown it will be negative (namely -1).
	 * @param filename The name the user provided for the file. Do with it what you will.
	 * @param contentType The content-type the user provided. This is optional and may very well be null.
	 * @return An application-specific reference to the data (this should almost never be a huge byte[] for memory reasons), or null if a non IO related error occurred.
	 */
	public D store(InputStream data, long length, String filename, String contentType) throws IOException;
}
