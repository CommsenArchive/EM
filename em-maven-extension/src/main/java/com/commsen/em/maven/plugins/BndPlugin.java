package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.PROP_ACTION_AUGMENT;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = BndPlugin.class)
public class BndPlugin extends DynamicMavenPlugin {

	
	public void addToBuild(MavenProject project) throws MavenExecutionException {

		String configuration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
				+ "<configuration>" //
				+ "  <bnd><![CDATA[ \n" //
				+ "		-exportcontents: ${packages;ANNOTATED;aQute.bnd.annotation.Export}\n" //
				+ "  ]]></bnd>" //
				+ "</configuration>"; //

		project.getBuild().getPlugins().add(0, preparePlugin(configuration));

		configureJarPlugin(project);
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
	}


	private Plugin preparePlugin(String configuration) throws MavenExecutionException {
		Plugin plugin = createPlugin( //
				"biz.aQute.bnd", //
				"bnd-maven-plugin", //
				"3.4.0-SNAPSHOT", //
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
