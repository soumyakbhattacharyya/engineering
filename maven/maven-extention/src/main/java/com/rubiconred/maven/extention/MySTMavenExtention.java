package com.rubiconred.maven.extention;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "resolve")
public class MySTMavenExtention extends AbstractMavenLifecycleParticipant {

	@Override
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
		// control won't come here
	}

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		System.out.println("MySTMavenExtention::afterProjectsRead : Goals to be executed -----> " + session.getGoals());
		System.out.println("MySTMavenExtention::afterProjectsRead : Execution root -----------> " + session.getExecutionRootDirectory());

		final String filePath = session.getExecutionRootDirectory();
		final List<String> goals = session.getGoals();	
		final List<MavenProject> initialListOfProjects = session.getProjects();

		// filter
		final List<MavenProject> finalListOfProjects = filter(initialListOfProjects, BaseCriteria.instance(goals, filePath));

		// set in session. note, THESE projects exclusively will be subjected to maven hereafter, whichever absent will be descoped from current execution session
		session.setProjects(finalListOfProjects);
	}

	/**
	 * filters src based on criteria
	 * @param src
	 * @param criteria
	 * @return modified list of projects 
	 */
	protected List<MavenProject> filter(List<MavenProject> src, Criteria criteria) {
		final List<MavenProject> dest = new ArrayList<MavenProject>();
		if ("".equals(criteria.getClause())) {
			// do not filter
			System.out.println("MySTMavenExtention::filter : not filtering original set of reactor projects");
			return src;
		} else if (".".equals(criteria.getClause())) {
			// return top most root project
			dest.add(src.get(src.size() - 1));
			System.out.println("MySTMavenExtention::filter : returning collection which should consists of parent project only : " + src.get(src.size() - 1));
			return dest;
		} else {
			final String[] modules = criteria.getClause().split(",");
			System.out.println(modules);			
			for (final MavenProject mavenProject : src) {
				System.out.println("MySTMavenExtention::filter : artifact id of the maven project : "+mavenProject.getArtifactId());
				if (contains2(modules, matchBy(mavenProject))) {
					dest.add(mavenProject);
				}
			}
			System.out.println("MySTMavenExtention::filter : returning collection which should consists of changed project(s) only : " + dest);
			return dest;
		}		
	}

	/***
	 * returns the matching clause from a maven project attributes 
	 * @param mavenProject
	 * @return group id + : + artifact id from maven project
	 */
	protected String matchBy(final MavenProject mavenProject) {
		return mavenProject.getGroupId()+":"+mavenProject.getArtifactId();
	}

	public interface Criteria {
		String getClause();
	}
	
	
	/** Base implementation for the Criteria interface*/	
	private static class BaseCriteria implements Criteria{
		
		private final List<String> goals;
		private final String filePath;
		
		public BaseCriteria(List<String> goals, String filePath) {
			this.goals = goals;
		    this.filePath = filePath;
		}

		public static Criteria instance(List<String> goals,String filePath){return new BaseCriteria(goals,filePath);}

		/** clause : a comma separated collection of strings, where each string represents a "group id:artifact id" combinations */
		public String getClause() {
			if (goals.contains("package") || goals.contains("install") || goals.contains("deploy")) {
				final File buildable_module = new File(filePath + File.separator + ".buildable_modules");
				if(buildable_module.exists()){
					// this should be a multi module project
					try {
						final String modules = FileUtils.fileRead(buildable_module,"UTF-8");
						return modules;
					} catch (final IOException e) {
						// throw an exception
						// and deliberately swallow it 
						e.printStackTrace();
					}
				}
				System.out.println("MySTMavenExtention::getClause : .buildable_modules does not exist");
			}
			// return empty String
			return "";
		
		}
		
	}
	
	/**
	 * finds if an array contains a certain element 
	 */
	private static <T> boolean contains2(final T[] array, final T v) {
	    if (v == null) {
	        for (final T e : array)
	            if (e == null)
	                return true;
	    } else {
	        for (final T e : array)
	            if (e == v || v.equals(e))
	                return true;
	    }

	    return false;
	}
	

}
