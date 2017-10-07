package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.VAL_EXTENSION_VERSION;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = EmRegisterContractPlugin.class)
public class EmRegisterContractPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(EmRegisterContractPlugin.class);


	public void addToPom(MavenProject project) throws MavenExecutionException {
		Plugin plugin = createPlugin("com.commsen.em", "em-maven-plugin", VAL_EXTENSION_VERSION, null, "registerContract", "registerContract", "package");
		project.getBuild().getPlugins().add(0, plugin);
		logger.info("Added `em-maven-plugin` to register contacts!");

	}


}
