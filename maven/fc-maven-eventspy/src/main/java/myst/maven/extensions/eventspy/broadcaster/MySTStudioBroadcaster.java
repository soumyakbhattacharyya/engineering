package myst.maven.extensions.eventspy.broadcaster;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import myst.maven.extensions.eventspy.MySTEventObserver;
import myst.maven.extensions.eventspy.broadcaster.internal.Endpoint;
import myst.maven.extensions.eventspy.broadcaster.internal.MySTStudio;
import myst.maven.extensions.eventspy.broadcaster.internal.Protocol;
import myst.maven.extensions.eventspy.model.JobState;

public class MySTStudioBroadcaster implements Broadcaster, PropertyChangeListener {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final String REQUEST_METHOD = Protocol.RequestMethod.PUT;
	
	
	private PropertyChangeEvent evt;
	 

	@Override
	public boolean broadcast() throws BroadcastException {
		LOGGER.info("MySTStudioBroadcaster :: broadcast : broadcasting to MyST Studio");
		try {
			
			final MySTStudio std = MySTStudio._default();
			LOGGER.info("Studio definition is following : {}",std);
			final Endpoint endpoint = Endpoint._default(std.getUrl(), std.getUserId(), std.getApiKey());
			LOGGER.info("Endpoint is following : {}",endpoint);
			final String payloadLication = MySTEventObserver.FINAL_PAYLOAD;
			LOGGER.info("Payload location is following : {}",payloadLication);
			
			endpoint.getProtocol().send(endpoint.getUrl(), endpoint.getFormat().serialize(readFrom(payloadLication)), endpoint.getTimeout(), endpoint.isJson(), REQUEST_METHOD);
			
		} catch (final Throwable e) {
			throw new BroadcastException("MySTStudioBroadcaster :: broadcast : issue occurred while broadcasting", e);
		}
		return SUCCESS;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		LOGGER.info("MySTStudioBroadcaster :: propertyChange : got notification");
		LOGGER.info("MySTStudioBroadcaster :: propertyChange : got environmental variable {}", System.getProperty("MYST_STUDIO_ENDPOINT")); 
		// set event
		this.evt = evt;
		// invoke broadcaster 
		broadcast();		
		LOGGER.info("MySTStudioBroadcaster :: propertyChange : returned after invoking broadcaster");
	}

	private JobState readFrom(String filePath) {
		LOGGER.info("reading payload.json file from following path {}", filePath);
		// Module module = null;
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (Reader reader = new FileReader(new File(filePath))) {
			final JobState jobState = gson.fromJson(reader, JobState.class);
			LOGGER.info("payload would consist of following data : {}",jobState);
			return jobState;
		} catch (final Exception e) {
			LOGGER.error("Exception", e);
			throw new RuntimeException(e);
		}
		// throw new AssertionError("impossible to reach a point where payload.json has not already created");
	}

}
