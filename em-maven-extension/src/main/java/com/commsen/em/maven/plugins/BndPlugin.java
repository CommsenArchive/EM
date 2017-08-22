package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.PROP_AUGMENT_FILE;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_IGNORE_PACKAGES;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_IMPORT_PACKAGES;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_INCLUDE_PACKAGES;
import static com.commsen.em.maven.extension.Constants.VAL_BND_VERSION;

import java.util.Arrays;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = BndPlugin.class)
public class BndPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(BndPlugin.class);

	
	public void addToBuild(MavenProject project) throws MavenExecutionException {

		String includePackages = project.getProperties().getProperty(PROP_CONFIG_INCLUDE_PACKAGES, "");
		String importPackages = project.getProperties().getProperty(PROP_CONFIG_IMPORT_PACKAGES, "");
		String ignorePackages = project.getProperties().getProperty(PROP_CONFIG_IGNORE_PACKAGES, "");
		
		StringBuilder importStatement = new StringBuilder();
		if (!ignorePackages.isEmpty()) {
			Arrays.stream(ignorePackages.split(",")).forEach(p -> importStatement.append("!").append(p).append(","));
		}
		if (!importPackages.isEmpty()) {
			importStatement.append(importPackages).append(",");
		}
		importStatement.append("*");
		
		StringBuilder configuration = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n") //
				.append("<configuration><bnd><![CDATA[ \n") //
				.append("	Private-Package:").append(includePackages).append("\n") //
				.append("	Import-Package: ").append(importStatement).append("\n") //
				.append("	-exportcontents: ${packages;ANNOTATED;aQute.bnd.annotation.Export}\n") //
				.append("]]></bnd></configuration>"); //

		logger.debug("Generated bnd-maven-plugin confgiguration: \n {}", configuration);
		
		project.getBuild().getPlugins().add(0, preparePlugin(configuration.toString()));

		configureJarPlugin(project);
		
		logger.info("Added `bnd-maven-plugin` to generate metadata");

	}


	public void addToBuildForAugment(MavenProject project) throws MavenExecutionException {

		String path = project.getProperties().getProperty(PROP_AUGMENT_FILE, "META-INF/augments.bnd");
		
		StringBuilder configuration = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n") //
				.append("<configuration><bnd><![CDATA[ \n") //
				.append("	Provide-Capability: bnd.augment; path='").append(path).append("'\n") //
				.append("]]></bnd></configuration>"); //

		logger.debug("Generated bnd-maven-plugin confgiguration: \n {}", configuration);

		project.getBuild().getPlugins().add(0, preparePlugin(configuration.toString()));

		configureJarPlugin(project);
		
		logger.info("Added 'bnd-maven-plugin' to genrate jar that augments (provides metadata for) other jars!");

	}


	private Plugin preparePlugin(String configuration) throws MavenExecutionException {
		Plugin plugin = createPlugin( //
				"biz.aQute.bnd", //
				"bnd-maven-plugin", //
				VAL_BND_VERSION, //
				configuration, //
				"bnd-process", //
				"bnd-process", null);
		return plugin;
	}
	
	
	private void configureJarPlugin(MavenProject project) throws MavenExecutionException {
		Plugin jarPlugin = getPlugin(project, "org.apache.maven.plugins:maven-jar-plugin");

		if (jarPlugin != null) {
			StringBuilder jarConfig = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n") //
					.append("<configuration><archive>\n") //
					.append("	<manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>\n") //
					.append("</archive></configuration>");

			configurePlugin(jarPlugin, "default-jar", jarConfig.toString());
		}
	}

}
