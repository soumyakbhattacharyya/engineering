package myst.maven.extensions.eventspy.model;

public class MavenModel {

	private String groupId;
	private String artifactId;

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public MavenModel(String groupId, String artifactId) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	@Override
	public String toString() {
		return "MavenModel [groupId=" + groupId + ", artifactId=" + artifactId + "]";
	}

}
