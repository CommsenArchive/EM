package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.util.Constants.PROP_AUGMENT_FILE;
import static com.commsen.em.maven.util.Constants.PROP_MODULE_IGNORE_PACKAGES;
import static com.commsen.em.maven.util.Constants.PROP_MODULE_IMPORT_PACKAGES;
import static com.commsen.em.maven.util.Constants.PROP_MODULE_INCLUDE_PACKAGES;
import static com.commsen.em.maven.util.Constants.VAL_BND_VERSION;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Constants;
import com.commsen.em.maven.util.Templates;

import freemarker.template.TemplateException;

@Component(role = BndPlugin.class)
public class BndPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(BndPlugin.class);

	@Requirement
	private Templates templates;
	
	public void addToBuild(MavenProject project) throws MavenExecutionException {

		String includePackages = project.getProperties().getProperty(PROP_MODULE_INCLUDE_PACKAGES, "");
		String importPackages = project.getProperties().getProperty(PROP_MODULE_IMPORT_PACKAGES, "");
		String ignorePackages = project.getProperties().getProperty(PROP_MODULE_IGNORE_PACKAGES, "");
		
		StringBuilder importStatement = new StringBuilder();
		if (!ignorePackages.isEmpty()) {
			Arrays.stream(ignorePackages.split(",")).forEach(p -> importStatement.append("!").append(p).append(","));
		}
		if (!importPackages.isEmpty()) {
			importStatement.append(importPackages).append(",");
		}
		importStatement.append("*");
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("includePackages", includePackages);
		model.put("importStatement", importStatement);
		
		String bndContent = null;
		try {
			bndContent = templates.process("META-INF/templates/bnd.fmt", model);
		} catch (IOException | TemplateException e) {
			throw new MavenExecutionException("Failed to process template file!", e);
		}
				
		StringBuilder configuration = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n") //
				.append("<configuration><bnd><![CDATA[ \n") //
				.append(bndContent)
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
					.append("<configuration>") //
					.append("	<archive>\n") //
					.append("		<manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>\n") //
					.append("	</archive>") //
					.append("	<annotationProcessorPaths>") //
					.append("		<annotationProcessorPath>") //
					.append("			<groupId>com.commsen.em</groupId>") //
					.append("			<artifactId>em.annotation.processors</artifactId>") //
					.append("			<version>").append(Constants.VAL_EXTENSION_VERSION).append("</version>") //
					.append("		</annotationProcessorPath>") //
					.append("	</annotationProcessorPaths>") //
					.append("</configuration>");

			configurePlugin(jarPlugin, "default-jar", jarConfig.toString());
		}
	}

}
