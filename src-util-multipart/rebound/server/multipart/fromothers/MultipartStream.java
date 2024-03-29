/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rebound.server.multipart.fromothers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import rebound.io.util.JRECompatIOUtilities;

//The core from the Apache File Upload project!  (that's all we need, no need for a dependency :> )

/**
 * <p>
 * Low level API for processing file uploads.
 * 
 * <p>
 * This class can be used to process data streams conforming to MIME 'multipart' format as defined in <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * 
 * <p>
 * The format of the stream is defined in the following way:<br>
 * 
 * <code>
 *   multipart-body ::= preamble encapsulation* close-delimiter epilogue<br>
 *   encapsulation ::= delimiter body CRLF<br>
 *   delimiter ::= "--" boundary CRLF<br>
 *   close-delimiter ::= "--" boudary "--"<br>
 *   body ::= header-part CRLF body-part &lt;making a double CRLF pair the end of headers&gt;<br>
 *   header-part ::= header*<br>
 *   header ::= header-name ":" header-value CRLF<br>
 *   header-name - Any ascii characters except ":"&gt;<br>
 *   header-value - Any ascii characters except CR & LF&gt;<br>
 *   body-data - arbitrary data that may not contain the boundary of this multipart stream or any enclosing multipart stream this is nested in<br>
 *   preamble - ignore<br>
 *   epilogue - ignore<br>
 * </code>
 * 
 * <p>
 * Note that body-data can contain another mulipart entity. There is limited support for single pass processing of such nested streams. The nested stream is <strong>required</strong> to have a boundary token of the same length as the parent stream (see {@link #setBoundary(byte[])}).
 * 
 * <p>
 * Here is an example of usage of this class.<br>
 * 
 * <pre>
 *    try {
 *        MultipartStream multipartStream = new MultipartStream(input,
 *                                                              boundary);
 *        boolean nextPart = multipartStream.skipPreamble();
 *        OutputStream output;
 *        while(nextPart) {
 *            header = chunks.readHeader();
 *            // process headers
 *            // create some output stream
 *            multipartStream.readBodyPart(output);
 *            nextPart = multipartStream.readBoundary();
 *        }
 *    } catch(MultipartStream.MalformedStreamException e) {
 *          // the stream failed to follow required syntax
 *    } catch(IOException) {
 *          // a read or write error occurred
 *    }
 * </pre>
 * 
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 * 
 * @version $Id: MultipartStream.java 551000 2007-06-27 00:59:16Z jochen $
 */
public class MultipartStream
{
	/**
	 * The Carriage Return ASCII character value.
	 */
	public static final byte CR = 0x0D;
	
	/**
	 * The Line Feed ASCII character value.
	 */
	public static final byte LF = 0x0A;
	
	/**
	 * The dash (-) ASCII character value.
	 */
	public static final byte DASH = 0x2D;
	
	/**
	 * The maximum length of <code>header-part</code> that will be processed. (64 kilobytes)
	 */
	public static final int DEFAULT_MAX_HEADER_SIZE = 65536;
	
	/**
	 * The default length of the buffer used for processing a request.
	 */
	public static final int DEFAULT_BUFSIZE = 4096;
	
	/**
	 * A byte sequence that marks the end of <code>header-part</code> (<code>CRLFCRLF</code>).
	 */
	protected static final byte[] HEADER_SEPARATOR = { CR, LF, CR, LF };
	
	/**
	 * A byte sequence that that follows a delimiter that will be followed by an encapsulation (<code>CRLF</code>).
	 */
	protected static final byte[] FIELD_SEPARATOR = { CR, LF };
	
	/**
	 * A byte sequence that that follows a delimiter of the last encapsulation in the stream (<code>--</code>).
	 */
	protected static final byte[] STREAM_TERMINATOR = { DASH, DASH };
	
	/**
	 * A byte sequence that precedes a boundary (<code>CRLF--</code>).
	 */
	protected static final byte[] BOUNDARY_PREFIX = { CR, LF, DASH, DASH };
	
	// ----------------------------------------------------------- Data members
	
	/**
	 * The input stream from which data is read.
	 */
	protected final InputStream input;
	
	/**
	 * The length of the boundary token plus the leading <code>CRLF--</code>.
	 */
	protected int boundaryLength;
	
	/**
	 * The amount of data, in bytes, that must be kept in the buffer in order to detect delimiters reliably.
	 */
	protected int keepRegion;
	
	/**
	 * The byte sequence that partitions the stream.
	 */
	protected byte[] boundary;
	
	/**
	 * The buffer used for processing the request.
	 */
	protected final byte[] buffer;
	
	/**
	 * The index of first valid character in the buffer. <br>
	 * 0 <= head < buffSize
	 */
	protected int head = 0;
	
	/**
	 * The index of last valid characer in the buffer + 1. <br>
	 * 0 <= tail <= bufSize
	 */
	protected int tail = 0;
	
	/**
	 * The content encoding to use when reading headers.
	 */
	protected String headerEncoding;
	
	protected int maxHeaderSize = DEFAULT_MAX_HEADER_SIZE;
	
//	/**
//	* The progress notifier, if any, or null.
//	*/
//	protected final ProgressNotifier notifier;
	
	// ----------------------------------------------------------- Constructors
	
	/**
	 * Creates a new instance.
	 */
	public MultipartStream()
	{
		this(null, DEFAULT_BUFSIZE);
	}
	
	/**
	 * Creates a new instance.
	 */
	public MultipartStream(InputStream in)
	{
		this(in, DEFAULT_BUFSIZE);
	}
	
	/**
	 * <p>
	 * Constructs a <code>MultipartStream</code> with a custom size buffer and no progress notifier.
	 * 
	 * <p>
	 * Note that the buffer must be at least big enough to contain the boundary string, plus 4 characters for CR/LF and double dash, plus at least one byte of data. Too small a buffer size setting will degrade performance.
	 * 
	 * @param input
	 *            The <code>InputStream</code> to serve as a data source.
	 * @param buffSize
	 *            The size of the buffer to be used, in bytes.
	 */
	public MultipartStream(InputStream input, int buffSize)
	{
		this.input = input;
		this.buffer = new byte[buffSize];
	}
	
	
	
	
	// --------------------------------------------------------- API methods
	
	/**
	 * Retrieves the character encoding used when reading the headers of an individual part. When not specified, or <code>null</code>, the platform default encoding is used.
	 * @return The encoding used to read part headers.
	 */
	public String getHeaderEncoding()
	{
		return headerEncoding;
	}
	
	
	
	/**
	 * Specifies the character encoding to be used when reading the headers of individual parts. When not specified, or <code>null</code>, the platform default encoding is used.
	 * 
	 * @param encoding
	 *            The encoding used to read part headers.
	 */
	public void setHeaderEncoding(String encoding)
	{
		headerEncoding = encoding;
	}

	
	
	
	/**
	 * Skips a <code>boundary</code> token, and checks whether more <code>encapsulations</code> are contained in the stream.
	 * 
	 * @return <code>true</code> if there are more encapsulations in this stream; <code>false</code> otherwise.
	 * 
	 * @throws MalformedStreamException
	 *             if the stream ends unexpecetedly or fails to follow required syntax.
	 */
	public boolean readBoundary() throws MalformedStreamException
	{
		byte[] marker = new byte[2];
		boolean nextChunk = false;
		
		head += boundaryLength;
		try
		{
			marker[0] = readByte();
			if (marker[0] == LF)
			{
				// Work around IE5 Mac bug with input type=image.
				// Because the boundary delimiter, not including the trailing
				// CRLF, must not appear within any file (RFC 2046, section
				// 5.1.1), we know the missing CR is due to a buggy browser
				// rather than a file containing something similar to a
				// boundary.
				return true;
			}
			
			marker[1] = readByte();
			if (arrayequals(marker, STREAM_TERMINATOR, 2))
			{
				nextChunk = false;
			}
			else if (arrayequals(marker, FIELD_SEPARATOR, 2))
			{
				nextChunk = true;
			}
			else
			{
				throw new MalformedStreamException("Unexpected characters follow a boundary");
			}
		}
		catch (IOException e)
		{
			throw new MalformedStreamException("Stream ended unexpectedly");
		}
		return nextChunk;
	}
	
	/**
	 * <p>
	 * Changes the boundary token used for partitioning the stream.
	 * 
	 * <p>
	 * This method allows single pass processing of nested multipart streams.
	 * 
	 * <p>
	 * The boundary token of the nested stream is <code>required</code> to be of the same length as the boundary token in parent stream.
	 * 
	 * <p>
	 * Restoring the parent stream boundary token after processing of a nested stream is left to the application.
	 * 
	 * @param boundary
	 *            The boundary to be used for parsing of the nested stream.
	 * 
	 * @throws IllegalBoundaryException
	 *             if the <code>boundary</code> has a different length than the one being currently parsed.
	 */
	public void setBoundary(byte[] boundary) throws IllegalBoundaryException
	{
		// We prepend CR/LF to the boundary to chop trailng CR/LF from
		// body-data tokens.
		this.boundary = new byte[boundary.length + BOUNDARY_PREFIX.length];
		this.boundaryLength = boundary.length + BOUNDARY_PREFIX.length;
		this.keepRegion = this.boundary.length;
		System.arraycopy(BOUNDARY_PREFIX, 0, this.boundary, 0, BOUNDARY_PREFIX.length);
		System.arraycopy(boundary, 0, this.boundary, BOUNDARY_PREFIX.length, boundary.length);
	}
	
	/**
	 * <p>
	 * Reads the <code>header-part</code> of the current <code>encapsulation</code>.
	 * 
	 * <p>
	 * Headers are returned verbatim to the input stream, including the trailing <code>CRLF</code> marker. Parsing is left to the application.
	 * 
	 * @return The <code>header-part</code> of the current encapsulation.
	 * 
	 * @throws MalformedStreamException
	 *             if the stream ends unexpecetedly.
	 */
	public String readHeaders() throws MalformedStreamException
	{
		int i = 0;
		byte b;
		// to support multi-byte characters
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int size = 0;
		while (i < HEADER_SEPARATOR.length)
		{
			try
			{
				b = readByte();
			}
			catch (IOException e)
			{
				throw new MalformedStreamException("Stream ended unexpectedly");
			}
			if (++size > maxHeaderSize)
			{
				throw new MalformedStreamException("Header section has more than " + maxHeaderSize + " bytes (maybe it is not properly terminated)");
			}
			if (b == HEADER_SEPARATOR[i])
			{
				i++;
			}
			else
			{
				i = 0;
			}
			baos.write(b);
		}
		
		String headers = null;
		if (headerEncoding != null)
		{
			try
			{
				headers = baos.toString(headerEncoding);
			}
			catch (UnsupportedEncodingException e)
			{
				// Fall back to platform default if specified encoding is not
				// supported.
				headers = baos.toString();
			}
		}
		else
		{
			headers = baos.toString();
		}
		
		return headers;
	}
	
	/**
	 * Creates a new {@link EncapsulationInputStream}.
	 * 
	 * @return A new instance of {@link EncapsulationInputStream}.
	 */
	public EncapsulationInputStream newInputStream() throws IOException
	{
		return new EncapsulationInputStream();
	}
	
	/**
	 * <p>
	 * Reads <code>body-data</code> from the current <code>encapsulation</code> and discards it.
	 * 
	 * <p>
	 * Use this method to skip encapsulations you don't need or don't understand.
	 * 
	 * @return The amount of data discarded.
	 * 
	 * @throws MalformedStreamException
	 *             if the stream ends unexpectedly.
	 * @throws IOException
	 *             if an i/o error occurs.
	 */
	public long discardBodyData() throws MalformedStreamException, IOException
	{
		return JRECompatIOUtilities.discard(newInputStream());
	}
	
	/**
	 * Finds the beginning of the first <code>encapsulation</code>.
	 * 
	 * @return <code>true</code> if an <code>encapsulation</code> was found in the stream.
	 * 
	 * @throws IOException
	 *             if an i/o error occurs.
	 */
	public boolean skipPreamble() throws IOException
	{
		// First delimiter may be not preceeded with a CRLF.
		System.arraycopy(boundary, 2, boundary, 0, boundary.length - 2);
		boundaryLength = boundary.length - 2;
		try
		{
			// Discard all data up to the delimiter.
			discardBodyData();
			
			// Read boundary - if succeded, the stream contains an
			// encapsulation.
			return readBoundary();
		}
		catch (MalformedStreamException e)
		{
			return false;
		}
		finally
		{
			// Restore delimiter.
			System.arraycopy(boundary, 0, boundary, 2, boundary.length - 2);
			boundaryLength = boundary.length;
			boundary[0] = CR;
			boundary[1] = LF;
		}
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Compares <code>count</code> first bytes in the arrays <code>a</code> and <code>b</code>.
	 * 
	 * @param a
	 *            The first array to compare.
	 * @param b
	 *            The second array to compare.
	 * @param count
	 *            How many bytes should be compared.
	 * 
	 * @return <code>true</code> if <code>count</code> first bytes in arrays <code>a</code> and <code>b</code> are equal.
	 */
	private static boolean arrayequals(byte[] a, byte[] b, int count)
	{
		for (int i = 0; i < count; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}
	
	
	/**
	 * Reads a byte from the <code>buffer</code>, and refills it as necessary.
	 * 
	 * @return The next byte from the input stream.
	 * 
	 * @throws IOException
	 *             if there is no more data available.
	 */
	protected byte readByte() throws IOException
	{
		// Buffer depleted ?
		if (head == tail)
		{
			head = 0;
			// Refill.
			tail = input.read(buffer, head, buffer.length);
			if (tail == -1)
			{
				// No more data available.
				throw new IOException("No more data is available");
			}
		}
		return buffer[head++];
	}
	
	
	/**
	 * Searches for a byte of specified value in the <code>buffer</code>, starting at the specified <code>position</code>.
	 * 
	 * @param value
	 *            The value to find.
	 * @param pos
	 *            The starting position for searching.
	 * 
	 * @return The position of byte found, counting from beginning of the <code>buffer</code>, or <code>-1</code> if not found.
	 */
	protected int findByte(byte value, int pos)
	{
		for (int i = pos; i < tail; i++)
			if (buffer[i] == value)
				return i;
		return -1;
	}
	
	/**
	 * Searches for the <code>boundary</code> in the <code>buffer</code> region delimited by <code>head</code> and <code>tail</code>.
	 * 
	 * @return The position of the boundary found, counting from the beginning of the <code>buffer</code>, or <code>-1</code> if not found.
	 */
	protected int findSeparator()
	{
		int first = 0;
		int match = 0;
		int maxpos = tail - boundaryLength;
		for (first = head; (first <= maxpos) && (match != boundaryLength); first++)
		{
			first = findByte(boundary[0], first);
			if (first == -1 || (first > maxpos))
			{
				return -1;
			}
			for (match = 1; match < boundaryLength; match++)
			{
				if (buffer[first + match] != boundary[match])
				{
					break;
				}
			}
		}
		
		if (match == boundaryLength)
			return first - 1;
		else
			return -1;
	}
	
	
	
	
	
	
	
	/**
	 * Thrown to indicate that the input stream fails to follow the required syntax.
	 */
	public static class MalformedStreamException
	extends IOException
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a <code>MalformedStreamException</code> with no detail message.
		 */
		public MalformedStreamException()
		{
			super();
		}
		
		/**
		 * Constructs an <code>MalformedStreamException</code> with the specified detail message.
		 * 
		 * @param message
		 *            The detail message.
		 */
		public MalformedStreamException(String message)
		{
			super(message);
		}
	}
	
	
	
	
	
	
	/**
	 * Thrown upon attempt of setting an invalid boundary token.
	 */
	public static class IllegalBoundaryException
	extends IOException
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructs an <code>IllegalBoundaryException</code> with no detail message.
		 */
		public IllegalBoundaryException()
		{
			super();
		}
		
		/**
		 * Constructs an <code>IllegalBoundaryException</code> with the specified detail message.
		 * 
		 * @param message
		 *            The detail message.
		 */
		public IllegalBoundaryException(String message)
		{
			super(message);
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * An {@link InputStream} for reading an encapsulation's contents.
	 */
	public class EncapsulationInputStream
	extends InputStream
	{
		/**
		 * The number of bytes, which have been read so far.
		 */
		protected long total;
		/**
		 * The number of bytes, which must be hold, because they might be a part of the boundary.
		 */
		protected int pad;
		/**
		 * The current offset in the buffer.
		 */
		protected int pos;
		/**
		 * Whether the stream is already closed.
		 */
		protected boolean closed;
		
		/**
		 * Creates a new instance.
		 */
		EncapsulationInputStream() throws IOException
		{
			findSeparator();
		}
		
		/**
		 * Called for finding the separator.
		 */
		protected void findSeparator()
		{
			pos = MultipartStream.this.findSeparator();
			if (pos == -1)
			{
				if (tail - head > keepRegion)
				{
					pad = keepRegion;
				}
				else
				{
					pad = tail - head;
				}
			}
		}
		
		/**
		 * Returns the number of bytes, which have been read by the stream.
		 * 
		 * @return Number of bytes, which have been read so far.
		 */
		public long getBytesRead()
		{
			return total;
		}
		
		/**
		 * Returns the number of bytes, which are currently available, without blocking.
		 * 
		 * @throws IOException
		 *             An I/O error occurs.
		 * @return Number of bytes in the buffer.
		 */
		public int available() throws IOException
		{
			if (pos == -1)
			{
				return tail - head - pad;
			}
			return pos - head;
		}
		
		/**
		 * Offset when converting negative bytes to integers.
		 */
		protected static final int BYTE_POSITIVE_OFFSET = 256;
		
		/**
		 * Returns the next byte in the stream.
		 * 
		 * @return The next byte in the stream, as a non-negative integer, or -1 for EOF.
		 * @throws IOException
		 *             An I/O error occurred or the stream is closed.
		 */
		public int read() throws IOException
		{
			if (closed)
			{
				throw new IOException();
			}
			if (available() == 0)
			{
				if (makeAvailable() == 0)
				{
					return -1;
				}
			}
			++total;
			int b = buffer[head++];
			if (b >= 0)
			{
				return b;
			}
			return b + BYTE_POSITIVE_OFFSET;
		}
		
		/**
		 * Reads bytes into the given buffer.
		 * 
		 * @param b
		 *            The destination buffer, where to write to.
		 * @param off
		 *            Offset of the first byte in the buffer.
		 * @param len
		 *            Maximum number of bytes to read.
		 * @return Number of bytes, which have been actually read, or -1 for EOF.
		 * @throws IOException
		 *             An I/O error occurred or the stream is closed.
		 */
		public int read(byte[] b, int off, int len) throws IOException
		{
			if (closed)
			{
				throw new IOException();
			}
			if (len == 0)
			{
				return 0;
			}
			int res = available();
			if (res == 0)
			{
				res = makeAvailable();
				if (res == 0)
				{
					return -1;
				}
			}
			res = Math.min(res, len);
			System.arraycopy(buffer, head, b, off, res);
			head += res;
			total += res;
			return res;
		}
		
		/**
		 * Closes the input stream.
		 * 
		 * @throws IOException
		 *             An I/O error occurred.
		 */
		public void close() throws IOException
		{
			if (closed)
			{
				return;
			}
			for (;;)
			{
				int av = available();
				if (av == 0)
				{
					av = makeAvailable();
					if (av == 0)
					{
						break;
					}
				}
				skip(av);
			}
			closed = true;
		}
		
		/**
		 * Skips the given number of bytes.
		 * 
		 * @param bytes
		 *            Number of bytes to skip.
		 * @return The number of bytes, which have actually been skipped.
		 * @throws IOException
		 *             An I/O error occurred or the stream is closed.
		 */
		public long skip(long bytes) throws IOException
		{
			if (closed)
			{
				throw new IOException();
			}
			int av = available();
			if (av == 0)
			{
				av = makeAvailable();
				if (av == 0)
				{
					return 0;
				}
			}
			long res = Math.min(av, bytes);
			head += res;
			return res;
		}
		
		/**
		 * Attempts to read more data.
		 * 
		 * @return Number of available bytes
		 * @throws IOException
		 *             An I/O error occurred.
		 */
		protected int makeAvailable() throws IOException
		{
			if (pos != -1)
			{
				return 0;
			}
			
			// Move the data to the beginning of the buffer.
			total += tail - head - pad;
			System.arraycopy(buffer, tail - pad, buffer, 0, pad);
			
			// Refill buffer with new data.
			head = 0;
			tail = pad;
			
			for (;;)
			{
				int bytesRead = input.read(buffer, tail, buffer.length - tail);
				if (bytesRead == -1)
				{
					// The last pad amount is left in the buffer.
					// Boundary can't be in there so signal an error
					// condition.
					throw new MalformedStreamException("Stream ended unexpectedly");
				}
				tail += bytesRead;
				
				findSeparator();
				int av = available();
				
				if (av > 0 || pos != -1)
				{
					return av;
				}
			}
		}
		
		/**
		 * Returns, whether the stream is closed.
		 * 
		 * @return True, if the stream is closed, otherwise false.
		 */
		public boolean isClosed()
		{
			return closed;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// ------------------------------------------------------ Debugging methods
	// These are the methods that were used to debug this stuff. //
	/**
	 * Main routine, for testing purposes only. Dump data.
	 * 
	 * @param args
	 *            A String[] with the command line arguments.
	 * @throws Exception,
	 *             a generic exception.
	 */
	
	public static void main(String[] args) throws Exception
	{
		String data = 
			"Hello in case you're using something out of the 1980's to view this ARPANET Text Message, welcome to the amazing world of MIME Multipart Data!--SANDSTONE\r\nContent-Type: application/octet-stream\r\n\r\n¬ˆ°Áªø•¥ø•ˆ¥˙ihløÁÓ89y7°·llty,ukgblo8y°·‡§¥ˆ¨˝Ól978687Ò¯ˆ©¨b‡§yujhgv\r\n--SANDSTONE\r\nContent-Disposition: form-data; filename=\"skiboarding.txt\"\r\n\r\nYesterday we went ski boarding, a nice break from all the sandstone I see back home. Oooh, creepy. Feel's like someone walked over my grave.\r\n--SANDSTONE--\r\nWell, for those of you dinosaurs, this concludes our MIME Multipart message. And just so ya'know, this little epilogue here makes it a hell of a lot more difficult to embed this multipart stream inside another multipart stream. But who needs to nest?!";
		
		String boundaryString =
			"SANDSTONE";
		
		MultipartStream parser = new MultipartStream(new ByteArrayInputStream(data.getBytes("ASCII")), (boundaryString.length()+4)+1);
		
		parser.setBoundary(boundaryString.getBytes("ASCII"));
		
		boolean continewe = parser.skipPreamble();
		
		while (continewe)
		{
			String header = parser.readHeaders();
			System.out.print("/*");
			System.out.print(header);
			System.out.print("*/");
			
			System.out.println();
			System.out.println();
			
			InputStream encapStream = parser.newInputStream();
			System.out.print("<--");
			JRECompatIOUtilities.pump(encapStream, System.out);
			System.out.print("-->");
			
			System.out.println("\n\n\n\n\n\n\n\n\n");
			
			continewe = parser.readBoundary();
		}
	}
}
