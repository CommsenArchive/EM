package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.VAL_BND_VERSION;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = BndIndexerPlugin.class)
public class BndIndexerPlugin extends DynamicMavenPlugin {

	public void addToPom(MavenProject project) throws MavenExecutionException {

		String configuration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
				+ "<configuration>" //
				+ "		<localURLs>ALLOWED</localURLs>" //
				+ "		<includeJar>true</includeJar>" //
				+ "		<outputFile>${project.build.directory}/index/index.xml</outputFile>" //
				+ "</configuration>"; //

		Plugin plugin = createPlugin("biz.aQute.bnd", "bnd-indexer-maven-plugin", VAL_BND_VERSION, configuration,
				"index", "index", null);

		project.getBuild().getPlugins().add(0, plugin);

	}

	public void configureForIndexGeneration(MavenProject project) throws MavenExecutionException {

		Plugin plugin = getPlugin(project, "biz.aQute.bnd:bnd-indexer-maven-plugin");

		if (plugin != null) {

			String configuration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
					+ "<configuration>" //
					+ "		<localURLs>ALLOWED</localURLs>" //
					+ "		<includeJar>false</includeJar>" //
					+ "		<outputFile>${project.build.directory}/index.xml</outputFile>" //
					+ "</configuration>"; //
			
			configurePlugin(plugin, "default-index", configuration);

		}
	}
}
