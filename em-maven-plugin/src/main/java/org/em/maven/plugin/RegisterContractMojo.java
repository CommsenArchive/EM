package org.em.maven.plugin;

import java.io.IOException;

import org.apache.maven.artifact.Artifact;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.contract.storage.ContractStorage;
import com.commsen.em.contract.storage.NitriteContractStorage;

/**
 * Goal which ...
 */
@Mojo(name = "registerContract", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class RegisterContractMojo extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(RegisterContractMojo.class);

	@Parameter( defaultValue = "${project}", readonly = true )
	MavenProject project;
	
	public void execute() throws MojoExecutionException {
		if (project.getPackaging() != "jar") {
			return;
		}
		Artifact artifact  = project.getArtifact();
		String contractorCoordinates = artifact.getGroupId()+":"+artifact.getArtifactId()+":"+artifact.getVersion();
		try {
			ContractStorage contractStorage = new NitriteContractStorage();
			contractStorage.saveContractor(artifact.getFile(), contractorCoordinates);
		} catch (IOException e) {
			logger.warn("Can not store contracts of " + contractorCoordinates, e);
		}
	}
}
