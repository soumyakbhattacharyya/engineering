package myst.maven.extensions.eventspy.broadcaster;

public class BroadcastException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BroadcastException(String message, Throwable cause) {
		super(message, cause);

	}

	public BroadcastException(String message) {
		super(message);
	}

}
