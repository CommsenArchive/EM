package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.PROP_ACTION_AUGMENT;
import static com.commsen.em.maven.extension.Constants.VAL_BND_VERSION;

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

		String configuration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
				+ "<configuration>" //
				+ "  <bnd><![CDATA[ \n" //
				+ "		-exportcontents: ${packages;ANNOTATED;aQute.bnd.annotation.Export}\n" //
				+ "  ]]></bnd>" //
				+ "</configuration>"; //

		project.getBuild().getPlugins().add(0, preparePlugin(configuration));

		configureJarPlugin(project);
		
		logger.info("Added `bnd-maven-plugin` to generate metadata");

	}


	public void addToBuildForAugment(MavenProject project) throws MavenExecutionException {

		String path = project.getProperties().getProperty(PROP_ACTION_AUGMENT+".file", "META-INF/augments.bnd");
		
		String configuration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
				+ "<configuration>" //
				+ "  <bnd><![CDATA[ \n" //
				+ "		Provide-Capability: bnd.augment; path='" + path + "'\n" //
				+ "  ]]></bnd>" //
				+ "</configuration>"; //


		project.getBuild().getPlugins().add(0, preparePlugin(configuration));

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
			String jarConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" //
					+ "<configuration>\n" //
					+ "  <archive>\n" //
					+ "    <manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>\n" //
					+ "   </archive>\n" //
					+ "</configuration>";

			configurePlugin(jarPlugin, "default-jar", jarConfig);
		}
	}

}
