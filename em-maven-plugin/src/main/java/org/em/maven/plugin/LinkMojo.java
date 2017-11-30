package org.em.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Dependencies;
import com.commsen.em.storage.PathsStorage;

/**
 * Goal which ...
 */
@Mojo(name = "link", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class LinkMojo extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(LinkMojo.class);

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File targetDir;

	@Component
	private Dependencies dependencies;

	@Component
	private PathsStorage pathsStorage;
	
	@Parameter(defaultValue="${localRepository}")
	ArtifactRepository localRepository;
	

	public void execute() throws MojoExecutionException {

		if ("jar".equals(project.getPackaging())) {
			try {
				
				Path m2Path = Paths.get(localRepository.getBasedir()).resolve(localRepository.pathOf(project.getArtifact()));
				Path projectPath = project.getExecutionProject().getArtifact().getFile().toPath();
				Path emProjectHome = com.commsen.em.maven.util.Constants.getHome(project);
				File f = project.getExecutionProject().getArtifact().getFile();
				Path emProjectPath = emProjectHome.resolve(f.getName());
				
				if (!emProjectPath.toFile().exists() && !Files.isSymbolicLink(emProjectPath)) {
					Files.createSymbolicLink(emProjectPath, f.toPath());
				}
				
				try {
					pathsStorage.savePaths(m2Path, projectPath, emProjectPath);
				} finally {
					pathsStorage.close();
				}
				
				logger.info("Created link to project's artifact in " + emProjectHome);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}
	}
}
