package myst.maven.extensions.eventspy.broadcaster;

/** broadcasts an event */
public interface Broadcaster {

	boolean SUCCESS = true;
	boolean FAILURE = false;

	boolean broadcast() throws BroadcastException;

}
