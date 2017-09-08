package com.commsen.em.maven.plugins;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;

import com.commsen.em.maven.util.MavenConfig;

public abstract class DynamicMavenPlugin {

	@Requirement
	MavenConfig mavenConfig;
	
	protected Plugin createPlugin(String groupId, String artifactId, String version, String configuration,
			String executionId, String goal, String phase) throws MavenExecutionException {
		Plugin plugin = new Plugin();
		plugin.setGroupId(groupId);
		plugin.setArtifactId(artifactId);
		plugin.setVersion(version);

		PluginExecution execution = new PluginExecution();
		execution.setId(executionId);
		execution.addGoal(goal);
		if (phase != null) {
			execution.setPhase(phase);
		}
		if (configuration != null) {
			execution.setConfiguration(mavenConfig.asXpp3Dom(configuration));
		}
		plugin.addExecution(execution);

		return plugin;
	}


	protected Plugin getPlugin(MavenProject project, String pluginGAV) {
		return project.getBuild().getPluginsAsMap().get(pluginGAV);
	}
	
	public void configurePlugin(Plugin plugin, String execution, String configuration) throws MavenExecutionException {
		if (plugin == null) return;
		if (execution == null || execution.trim().isEmpty()) return;
		plugin.getExecutionsAsMap().get(execution).setConfiguration(mavenConfig.asXpp3Dom(configuration));
	}
	
	
}
