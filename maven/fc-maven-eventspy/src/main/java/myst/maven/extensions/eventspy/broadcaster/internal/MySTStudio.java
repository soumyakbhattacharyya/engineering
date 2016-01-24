package myst.maven.extensions.eventspy.broadcaster.internal;

public class MySTStudio {

	private static final long serialVersionUID = 1L;

	/** logical name */
	private String installationIdentifier;

	/** endpoint */
	private String url;
	private String userId;
	private String apiKey;

	public static MySTStudio _default() {
		// TODO : change below mentioned fictitious constructor to something
		// more meaningful
		final MySTStudio studio = new MySTStudio("default", "http://demo4148143.mockable.io/myst-endpoint", "myst-api-client","reallystrongandencryptedapikey");
		return studio;
	}

	public MySTStudio(String installationIdentifier, String url, String userId, String apiKey) {
		super();
		this.installationIdentifier = installationIdentifier;
		this.url = url;
		this.userId = userId;
		this.apiKey = apiKey;
	}

	public String getInstallationIdentifier() {
		return installationIdentifier;
	}

	public void setInstallationIdentifier(String installationIdentifier) {
		this.installationIdentifier = installationIdentifier;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}
