package org.em.maven.plugin;

import java.util.Arrays;
import java.util.List;

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
import org.beryx.textio.InputReader.ValueChecker;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;

/**
 * Goal which ...
 */
@Mojo(name = "create", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresProject = false)
public class CreateMojo extends AbstractMojo {

	@Parameter( defaultValue = "${project}", readonly = true )
	MavenProject project;
	
	private static final String DEFAULT_ID = "org.apache.maven:standalone-pom:pom:1";
	
	private TextIO textIO = TextIoFactory.getTextIO();
	
	public void execute() throws MojoExecutionException {

		if (DEFAULT_ID.equals(project.getId())) {
			createProject();
		} else {
			createModule();
		}
	}
	
	private void createProject () throws MojoExecutionException {
		textIO.getTextTerminal().print("JUST A MESSAGE");
		
		String project = textIO.newStringInputReader()
		        .withDefaultValue("com.commsen.em.generated:generated-project:1.0.0-SNAPSHOT")
		        .withValueChecker(new ValueChecker<String> () {
					@Override
					public List<String> getErrorMessages(String val, String itemName) {
						if (val.matches("^.*:.*:.*$")) return null;
						return Arrays.asList("Id should be 'group:artifact:version' ");
					}
		        })
		        .read("project ID");
		
		boolean multimodule = textIO.newBooleanInputReader()
			.withDefaultValue(false)
			.read("Multi-module project");
		
		if (multimodule) {
			List<String> modules = textIO.newStringInputReader()
				.readList("module IDs", "what is this for", "interesting");
			for (String string : modules) {
				System.out.println("Module: " + string);
			}
			
		}
	}

	private void createModule () throws MojoExecutionException {
		System.err.println("For now creating modules is not supported!");
	}

}
