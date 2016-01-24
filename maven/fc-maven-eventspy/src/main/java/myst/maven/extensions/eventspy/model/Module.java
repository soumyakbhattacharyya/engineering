package myst.maven.extensions.eventspy.model;

import java.util.ArrayList;
import java.util.List;

public class Module {

	public static class Builder {

		protected final String artifactId;
		protected final String componentType;
		protected String description;
		protected final String groupId;
		protected String name;
		protected final String binaryRepositoryUrl;

		public Builder(String groupId, String artifactId, String componentType, String binaryRepositoryUrl) {
			super();
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.componentType = componentType;
			this.binaryRepositoryUrl = binaryRepositoryUrl;
		}

		public Module build() {
			return new Module(this);
		}

		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

	}

	protected String buildStatus = "NOT_BUILT";
	private List<Module> children;
	protected String componentType;
	protected String description;
	protected Module parent;
	protected MavenModel mavenModel;
	protected String binaryRepositoryUrl;

	protected String name;

	// Stringified boolean signifying is the Module is topLevel
	protected boolean topLevel;

	protected Module() {
	}

	protected Module(Builder builder) {
		this.mavenModel = new MavenModel(builder.groupId, builder.artifactId);
		// this.version = builder.version;
		this.name = builder.name;
		this.description = builder.description;
		this.componentType = builder.componentType;
		this.binaryRepositoryUrl = builder.binaryRepositoryUrl;
	}

	public String getBinaryRepositoryUrl() {
		return binaryRepositoryUrl;
	}

	public void setBinaryRepositoryUrl(String binaryRepositoryUrl) {
		this.binaryRepositoryUrl = binaryRepositoryUrl;
	}

	public void addChild(Module module) {
		if(children == null){
			children = new ArrayList<Module>();
		}
		children.add(module);
	}

	
	public String getBuildStatus() {
		return buildStatus;
	}

	public String getComponentType() {
		return componentType;
	}

	public String getDescription() {
		return description;
	}

	public MavenModel getMavenModel() {
		return mavenModel;
	}

	public String getName() {
		return name;
	}

	public List<Module> getSubmodules() {
		return children;
	}

	public boolean getTopLevel() {
		return topLevel;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMavenModel(MavenModel mavenModel) {
		this.mavenModel = mavenModel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSubmodules(List<Module> submodules) {
		this.children = submodules;
	}

	public void setTopLevel(boolean topLevel) {
		this.topLevel = topLevel;
	}

	@Override
	public String toString() {
		return "Module [buildStatus=" + buildStatus + ", children=" + children + ", componentType=" + componentType
				+ ", description=" + description + ", parent=" + parent + ", mavenModel=" + mavenModel
				+ ", binaryRepositoryUrl=" + binaryRepositoryUrl + ", name=" + name + ", topLevel=" + topLevel + "]";
	}
	
	

}
