package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.util.Constants.VAL_EXTENSION_VERSION;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Templates;

@Component(role = LinkPlugin.class)
public class LinkPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(LinkPlugin.class);

	@Requirement
	private Templates templates;
	
	public void addToBuild(MavenProject project) throws MavenExecutionException {

		project.getBuild().getPlugins().add(0, preparePlugin());
		logger.info("Added `em-maven-plugin:link` to keep track of modules");
	}

	private Plugin preparePlugin() throws MavenExecutionException {
		Plugin plugin = createPlugin( //
				"com.commsen.em", //
				"em-maven-plugin", //
				VAL_EXTENSION_VERSION, //
				null, //
				"link", //
				"link", //
				"package");
		return plugin;
	}
	

}
