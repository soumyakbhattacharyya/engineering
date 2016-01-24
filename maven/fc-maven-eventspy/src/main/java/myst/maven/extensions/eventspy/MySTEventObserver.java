package myst.maven.extensions.eventspy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.inject.Named;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.DependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.eclipse.aether.RepositoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cedarsoftware.util.Traverser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import myst.maven.extensions.eventspy.broadcaster.Broadcaster;
import myst.maven.extensions.eventspy.broadcaster.MySTStudioBroadcaster;
import myst.maven.extensions.eventspy.model.JobState;
import myst.maven.extensions.eventspy.model.Module;

@Named
public class MySTEventObserver extends AbstractEventSpy {

	public static final String NOT_BUILT = "NOT_BUILT";
	public static final String BUILT = "BUILT";
	public static final String INITIAL_PROJECT_STRUCTURE = getOutputDir() + File.separator + "structure-v1.0.json";
	public static final String UPDATED_PROJECT_STRUCTURE_WITH_COMPONENT_TYPE = getOutputDir() + File.separator + "structure-v2.0.json";
	public static final String FINAL_PAYLOAD = getOutputDir() + File.separator + "payload.json";
	public static final String[] SUPPORTED_TYPES = new String[] { "MDS", "OSB", "SCA", "JDEV", "JAVA" };
	public static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd 'at' HH:mm:ss z";

	/**
	 * discover location to create / read files
	 * 
	 * @return location
	 */
	private static String getOutputDir() {
		final String location = System.getProperty("custom.output.dir");
		if (location != null && !location.isEmpty()) {
			return location;
		}

		throw new AssertionError(
				"it is mandatory to mention the destination where project structure file would be written, use -Dcustom.output.dir for the same");
	}
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	// references
	private Module moduleDef;
	private String payloadLocation = "";

	/**
	 * goal, associated with a session, is important, as it effects control flow 
	 */
	private List<String> goals;

	public MySTEventObserver() {
		LOGGER.info("********************************************");
		LOGGER.info("Creating new instance of  MySTEventObserver ");
		LOGGER.info("********************************************");

	}

	/**
	 * register broadcasters
	 */
	private void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void close() {

		/**
		 * creates outcome files
		 */

		try {
			// if structure has not to be discovered
			// and there exists updated project file
			if (this.moduleDef != null && goals.contains("process-sources")) {
				createUpdatedStructureWithComponentType();
			} else if (doesExist(UPDATED_PROJECT_STRUCTURE_WITH_COMPONENT_TYPE) && goals.contains("package")) {
				createFinalPayload();
			} else if (doesExist(FINAL_PAYLOAD) && goals.contains("deploy")){
				// this is where you should try to let MyST Studio know
				final Broadcaster broadcaster = new MySTStudioBroadcaster();
				broadcaster.broadcast();
			}
		} catch (final Throwable e) {
			LOGGER.error("Exception", e);
			throw new RuntimeException(e);
		}

		LOGGER.info("MySTEventObserver: Observation completed");
	}

	/**
	 * creates final payload 
	 * 
	 * @throws Exception
	 */
	private void createFinalPayload() throws Exception {
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// irrespective of the fact that this processing is happening for single / multi module,
		// set moduleDef's  top level flag to true
		moduleDef.setTopLevel(true);
		
		// figure out if parents need to marked as built 
		findIfParentShouldBeDeclaredBuilt(moduleDef, null);
		
		// create wrapper 
		final JobState jobState = new JobState();
		jobState.setBuildDetail(moduleDef);
		jobState.setBuildNumber(getBuildNumber());
		jobState.setStartTime(getStartTime());
		jobState.setEndTimestamp(Calendar.getInstance(), new SimpleDateFormat(DEFAULT_DATE_FORMAT));
		jobState.setStatus("SUCCESS"); // TODO : need to find out status 
		

		try (Writer writer = new FileWriter(FINAL_PAYLOAD)) {
			gson.toJson(jobState, writer);
			LOGGER.info("MySTEventObserver: constructed payload file");			
		} catch (final Exception e) {
			LOGGER.error("Exception", e);
			throw e;
		}
		
		this.setPayloadLocation(FINAL_PAYLOAD);
	}
	
	/**
	 * creates file that shows how the project is organized without mentioning component type etc.
	 * 
	 * @param mavenProject
	 */
	private void createInitialStructure(MavenProject mavenProject){
		final MavenProject root = mavenProject;
		final Module module = new Module.Builder(root.getGroupId(), root.getArtifactId(), "TBD", "TBD").withName(root.getName()).withDescription(root.getDescription()).build();
		findSubModules(root, root.getBasedir().getAbsolutePath(), module);
		try {
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			if (module != null) {
				LOGGER.info("creating initial structure");

				// if the file exists remove it
				final File oldStructureJson = new File(INITIAL_PROJECT_STRUCTURE);
				if (oldStructureJson.exists()) {
					(oldStructureJson).delete();
				}

				// write structure afresh
				try (Writer writer = new FileWriter(INITIAL_PROJECT_STRUCTURE)) {
					gson.toJson(module, writer);
				} catch (final Exception e) {
					LOGGER.error("Exception", e);
					throw e;
				}
			} else {
				LOGGER.info("could not find model instance to write to structure.json");
			}
		} catch (final Throwable e) {
			LOGGER.error("Throwable", e);
			throw new RuntimeException(e);
		}
	}
	
	/** create a new version of structure.json, with component type updated */
	private void createUpdatedStructureWithComponentType() throws Exception {
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (moduleDef != null) {
			LOGGER.info(
					"MySTEventObserver::executionResultEventHandler : will attempt to create updated project structure with component type updated");

			// if the file exists remove it
			final File oldStructureJson = new File(UPDATED_PROJECT_STRUCTURE_WITH_COMPONENT_TYPE);
			if (oldStructureJson.exists()) {
				(oldStructureJson).delete();
			}

			// write structure afresh
			try (Writer writer = new FileWriter(UPDATED_PROJECT_STRUCTURE_WITH_COMPONENT_TYPE)) {
				gson.toJson(moduleDef, writer);
				LOGGER.info("MySTEventObserver::executionResultEventHandler : done with creating");
			} catch (final Exception e) {
				LOGGER.error("Exception", e);
				throw e;
			}
		}
	}
	
	
	/** NOT USED */
	private void dependencyResolutionRequestEvent(DependencyResolutionRequest event) {
		LOGGER.info("MySTEventObserver::dependencyResolutionRequestEvent", event);
	}

	/** NOT USED */
	private void dependencyResolutionResultEvent(DependencyResolutionResult event) {
		LOGGER.info("MySTEventObserver::dependencyResolutionResultEvent", event);
	}

	private boolean doesExist(File file){ return file.exists();}
	private boolean doesExist(String path){ return doesExist(new File(path));}

	private void executionEventHandler(ExecutionEvent executionEvent) {
		
		// identify the goal
		final List<String> goals = getGoals(executionEvent.getSession());
		
		// initialize 
		MavenProject mavenProject = null;
		String groupId = "";
		String artifactId = "";
		String version = "";
		String componentType = "";
		
				
		switch (executionEvent.getType()) {
		case SessionEnded:
			break;
		case ForkFailed:
			break;
		case ForkStarted:
			break;
		case ForkSucceeded:
			break;
		case ForkedProjectFailed:
			break;
		case ForkedProjectStarted:
			break;
		case ForkedProjectSucceeded:
			break;
		case MojoFailed:
			break;
		case MojoSkipped:
			break;
		case MojoStarted:
			break;
		case MojoSucceeded:
			break;
		case ProjectDiscoveryStarted:
			break;
		case ProjectFailed:
			LOGGER.info("project {} failed", executionEvent.getProject());
			
			mavenProject = executionEvent.getProject();
			groupId = mavenProject.getGroupId();
			artifactId = mavenProject.getArtifactId();
			version = mavenProject.getVersion();
			componentType = findComponentType(mavenProject.getParent());
			
			if (goals.contains("package")) {
				if (moduleDef == null) {
					readJson(UPDATED_PROJECT_STRUCTURE_WITH_COMPONENT_TYPE);
				}
				updateWithBuildOutcome(groupId, artifactId, version, componentType);
			}
			break;
		case ProjectSkipped:
			LOGGER.info("project {} skipped", executionEvent.getProject());
			break;
		case ProjectStarted:
			LOGGER.info("project {} started", executionEvent.getProject());
			break;
		case ProjectSucceeded:
			LOGGER.info("project {} succeeded", executionEvent.getProject());
			mavenProject = executionEvent.getProject();
			groupId = mavenProject.getGroupId();
			artifactId = mavenProject.getArtifactId();
			version = mavenProject.getVersion();
			
			componentType = findComponentType(mavenProject.getParent());
			LOGGER.info("parent discovered {}",mavenProject.getParent());
			LOGGER.info("component type discovered {}",componentType);
			
			if (goals.contains("process-sources")) {
				if (!doesExist(INITIAL_PROJECT_STRUCTURE)) {
					createInitialStructure(mavenProject);
				} else {
					if (moduleDef == null) {
						readJson(INITIAL_PROJECT_STRUCTURE);
					}
					updateWithComponentType(groupId, artifactId, version, componentType);
				}
			}
			
			if (goals.contains("package")) {
				if (moduleDef == null) {
					readJson(UPDATED_PROJECT_STRUCTURE_WITH_COMPONENT_TYPE);
				}
				updateWithBuildOutcome(groupId, artifactId, version, componentType);
			}
			
			break;
		case SessionStarted:
			break;
		default:
			LOGGER.info("MySTEventObserver::executionEventHandler(UNKNOWN) {}", executionEvent.getType().name());
			break;
		}
	}

	

	private void executionRequestEventHandler(MavenExecutionRequest event) {
		LOGGER.info("MySTEventObserver::executionRequestEventHandler : request has been received to build project {} ",	event.getSelectedProjects());		
	}

	/**
	 * creates initial structure 
	 * 
	 * @param event
	 */
	private void executionResultEventHandler(MavenExecutionResult event) {

	}

	/**
	 * finds component type by introspecting parent pom
	 * 
	 * @param parent
	 * @return
	 */
	private String findComponentType(MavenProject parent) {
		LOGGER.info("finding parent");
		if (null == parent)
			return "NO_PARENT";
		final String parentArtifactId = parent.getArtifactId();
		if (parentArtifactId.contains("mds"))
			return "MDS";
		if (parentArtifactId.contains("osb"))
			return "OSB";
		if (parentArtifactId.contains("jdev"))
			return "JAVA";
		if (parentArtifactId.contains("sca"))
			return "SCA";
		if (parentArtifactId.contains("build"))
			return "POM";
		// if noting matches return OTHERS, BUT raise an warning as this is not
		// expected
		LOGGER.warn("MySTEventObserver::findComponentType : no parent found !!!");
		return "OTHERS";
	}

	private void findIfParentShouldBeDeclaredBuilt(Module _this, Module parent) {
		final List<Module> modules = _this.getSubmodules();
		// a single module case
		if (null == modules || modules.size() == 0) {
			return;
		}

		for (final Module submodule : modules) {
			LOGGER.debug("Submodule {}", submodule);
			// if any of the concrete submodule has got built
			// mark parent as built and break
			if (Arrays.asList(SUPPORTED_TYPES).contains(submodule.getComponentType())
					&& BUILT.equals(submodule.getBuildStatus())) {
				// mark this instance as built
				_this.setBuildStatus(BUILT);
				// mark parent as built
				if(parent != null){
					parent.setBuildStatus(BUILT);
				}
			} else {
				findIfParentShouldBeDeclaredBuilt(submodule, _this);
			}
		}
	}

	private void findSubModules(MavenProject root, String rootDir, Module parent) {
		// for single module or leaf level modules of a multi module maven
		// project
		if (root.getModules().size() == 0)
			return;
		// Module module = new Module.Builder(root.getGroupId(),
		// root.getArtifactId(), "TBD", "TBD").build();
		for (final String submodulePath : root.getModules()) {
			LOGGER.info("MySTEventObserver::findSubModules : sub module location : {}", submodulePath);
			Model model;
			try {
				model = pomToModel(rootDir + "/" + submodulePath + "/pom.xml");
				final Module submodule = new Module.Builder(model.getGroupId(), model.getArtifactId(), "TBD", "TBD").withName(model.getName()).withDescription(model.getDescription()).build();
				parent.addChild(submodule);
				final MavenProject subMavenProject = new MavenProject(model);
				findSubModules(subMavenProject, rootDir + "/" + submodulePath, submodule);
			} catch (final Exception e) {
				LOGGER.error("Exception", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * get build number
	 * @return
	 */
	private String getBuildNumber() {
		return System.getProperty("build.number");
	}

	/**
	 * gets goals
	 * 
	 * @param current session
	 * @return goals
	 */
	private List<String> getGoals(MavenSession session) {
		if (null == goals) {
			this.goals = session.getGoals();
			return this.goals;
		} else
			return this.goals;
	}
	
	public String getPayloadLocation() {
		return payloadLocation;
	}

	private String getStartTime() {
		return System.getProperty("build.start.time");
	}

	@Override
	public void init(Context context) throws Exception {
		LOGGER.info("MySTEventObserver::init() : reached initialization state");
	}

    private boolean moduleMatchesCoordinates(final String groupId, final String artifactId, Module module) {
		return module.getMavenModel().getGroupId().equals(groupId)
				&& module.getMavenModel().getArtifactId().equals(artifactId);
	}

	/**
	 * Notifies the spy of some build event/operation.
	 * 
	 * @param event
	 *            The event, never {@@code null}.
	 * @see org.apache.maven.settings.building.SettingsBuildingRequest
	 * @see org.apache.maven.settings.building.SettingsBuildingResult
	 * @see org.apache.maven.execution.MavenExecutionRequest
	 * @see org.apache.maven.execution.MavenExecutionResult
	 * @see org.apache.maven.project.DependencyResolutionRequest
	 * @see org.apache.maven.project.DependencyResolutionResult
	 * @see org.apache.maven.execution.ExecutionEvent
	 * @see org.sonatype.aether.RepositoryEvent
	 */
	@Override
	public void onEvent(Object event) throws Exception {
		
		try {

			if (event instanceof ExecutionEvent) {
				// during execution event 
				executionEventHandler((ExecutionEvent) event);
			} else if (event instanceof SettingsBuildingRequest) {
			} else if (event instanceof SettingsBuildingResult) {
				// invoked when setting has been built
				// settingsBuilderResultEvent((SettingsBuildingResult) event);
			} else if (event instanceof MavenExecutionRequest) {
				// invoked when request to start an execution has reached
				// executionRequestEventHandler((MavenExecutionRequest) event);
			} else if (event instanceof MavenExecutionResult) {
				// invoked when a result of a execution has reached
				// executionResultEventHandler((MavenExecutionResult) event);
			} else if (event instanceof DependencyResolutionRequest) {
			} else if (event instanceof DependencyResolutionResult) {
			} else if (event instanceof RepositoryEvent) {
			}
		} catch (final Exception e) {
			LOGGER.error("Exception", e);
			throw new RuntimeException(e);
		}
	}
	
	private Model pomToModel(String pathToPom) throws Exception {
		final BufferedReader in = new BufferedReader(new FileReader(pathToPom));
		final MavenXpp3Reader reader = new MavenXpp3Reader();
		final Model model = reader.read(in);
		return model;
	}
	private void readJson(String path) {
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		LOGGER.info("MySTEventObserver::readJson : reading json from following location {}", path);
		try (Reader reader = new FileReader(new File(path))) {
			moduleDef = gson.fromJson(reader, Module.class);
			LOGGER.debug("MySTEventObserver::readJson : module constructed with following detail {}", moduleDef);
			// return structureModule;
		} catch (final Exception e) {
			LOGGER.error("Exception", e);
			throw new RuntimeException(e);
		}
	}

	/** NOT USED */
	private void repositoryEvent(RepositoryEvent event) {
		LOGGER.info("MySTEventObserver::repositoryEvent {}", event.getType());
	}

	public void setPayloadLocation(String payloadLocation) {
		if ((new File(payloadLocation).exists())) {
			// broadcast this event
			pcs.firePropertyChange("payloadLocation", this.payloadLocation, payloadLocation);
		}
		// set payload location
		this.payloadLocation = payloadLocation;

	}

	/** NOT USED */
	private void settingsBuilderRequestEvent(SettingsBuildingRequest event) {
		LOGGER.info("MySTEventObserver::settingsBuilderEvent", event);
	}

	private void settingsBuilderResultEvent(SettingsBuildingResult event) {
		LOGGER.info("MySTEventObserver::settingsBuilderResultEvent : settings building has completed");
	}

	private void updateWithBuildOutcome(final String groupId, final String artifactId, final String version,
			final String componentType) {
		// find component type from the parent pom of the project
		if (moduleDef != null) {
			LOGGER.info("MySTEventObserver::updateWithBuildOutcome : updating final payload with build outcome");
			final Traverser.Visitor visitor = new Traverser.Visitor() {
				@Override
				public void process(Object o) {
					if (o instanceof Module) {
						final Module module = (Module) o;
						if (moduleMatchesCoordinates(groupId, artifactId, module)) {
							module.setBuildStatus(BUILT);
							module.setBinaryRepositoryUrl(version);
							module.setComponentType(componentType);
						}
					}
				}
			};
			Traverser.traverse(moduleDef, visitor);
		}
	}

	private void updateWithComponentType(final String groupId, final String artifactId, final String version,
			final String componentType) {
		final Traverser.Visitor visitor = new Traverser.Visitor() {
			@Override
			public void process(Object o) {
				if (o instanceof Module) {
					final Module module = (Module) o;
					if (moduleMatchesCoordinates(groupId, artifactId, module)) {
						LOGGER.info("setting component type");
						module.setBuildStatus(NOT_BUILT);
						module.setBinaryRepositoryUrl(version);
						module.setComponentType(componentType);
					}
				}
			}
		};
		Traverser.traverse(moduleDef, visitor);
	}

}
