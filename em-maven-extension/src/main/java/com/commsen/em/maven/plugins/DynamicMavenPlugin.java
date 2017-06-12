package com.commsen.em.maven.plugins;

import java.io.StringReader;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

public abstract class DynamicMavenPlugin {

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
			execution.setConfiguration(config2dom(configuration));
		}
		plugin.addExecution(execution);

		return plugin;
	}

	protected Xpp3Dom config2dom(String config) throws MavenExecutionException {
		try {
			return Xpp3DomBuilder.build(new StringReader(config));
		} catch (Exception e) {
			throw new MavenExecutionException("Error parsing config", e);
		}
	}

	protected Plugin getPlugin(MavenProject project, String pluginGAV) {
		return project.getBuild().getPluginsAsMap().get(pluginGAV);
	}
	
	
	protected void configurePlugin(Plugin plugin, String execution, String configuration) throws MavenExecutionException {
		if (plugin == null) return;
		if (execution == null || execution.trim().isEmpty()) return;
		plugin.getExecutionsAsMap().get(execution).setConfiguration(config2dom(configuration));
	}
	
}
