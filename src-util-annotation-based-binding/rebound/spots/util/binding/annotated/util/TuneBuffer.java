/*
 * Created on Dec 29, 2008
 * 	by the great Eclipse(c)
 */
package rebound.spots.util.binding.annotated.util;

/**
 * <p>Indicates that the action bean requires an abnormal response buffer size.</p>
 * 
 * There are two levels of defaults here:
 * <ol>
 * 	<li>An ActionBean has no <code>TuneBuffer</code> annotation at all    -    {@link #DEFAULT_RESPONSE_BUFFER_SIZE_NOFLUSHPENDING} / {@link #DEFAULT_RESPONSE_BUFFER_SIZE_YESFLUSHPENDING} is used.</li>
 * 	<li>An ActionBean has <code>TuneBuffer</code>, but does not specify the {@link #value()} attribute   -   Default of {@link #value()} is used (1048576)</li>
 * 	<li>An ActionBean has <code>TuneBuffer</code>, and does explicitly specify the {@link #value()}   -   The specified value is used.</li>
 * </ol>
 * 
 * This means that simply tagging a class with <code>TuneBuffer</code> is equivalent to saying: "This action should have an extra-large buffer."
 * @author RProgrammer
 */
public @interface TuneBuffer
{
	/**
	 * This is what every HTTP response's buffer is set to which does not go to an action bean with this annotation.
	 */
	public static final int DEFAULT_RESPONSE_BUFFER_SIZE_YESFLUSHPENDING = 1024 * 1024;   //1 MB
	public static final int DEFAULT_RESPONSE_BUFFER_SIZE_NOFLUSHPENDING = 32 * 1024;   //32 kB
	
	/**
	 * The requested size of the buffer in bytes.  Note that this is merely a suggestion, the actual buffer might not be of exactly the size requested.
	 * The default is one megabyte (1048576 bytes).
	 */
	int value() default DEFAULT_RESPONSE_BUFFER_SIZE_YESFLUSHPENDING;
}
