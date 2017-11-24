package org.em.maven.plugin;

import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.contract.storage.ContractStorage;

/**
 * Goal which ...
 */
@Mojo(name = "registerContract", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class RegisterContractMojo extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(RegisterContractMojo.class);

	@Parameter(defaultValue = "${project}", readonly = true)
	MavenProject project;

	public void execute() throws MojoExecutionException {
		if (project.getPackaging() != "jar") {
			return;
		}
		Artifact artifact = project.getArtifact();
		String contractorCoordinates = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
				+ artifact.getVersion();
		try (ContractStorage contractStorage = ContractStorage.instance()) {
			contractStorage.saveContractor(artifact.getFile(), contractorCoordinates);
		} catch (IOException e) {
			logger.warn("Can not store contracts of " + contractorCoordinates, e);
		}
	}
}
