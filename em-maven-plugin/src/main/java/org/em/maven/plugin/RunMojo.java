package org.em.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Dependencies;

import aQute.bnd.build.ProjectLauncher;
import aQute.bnd.build.Workspace;
import aQute.bnd.osgi.Constants;
import aQute.bnd.repository.fileset.FileSetRepository;
import aQute.bnd.service.RepositoryPlugin;
import aQute.lib.io.IO;
import biz.aQute.resolve.Bndrun;

/**
 * Goal which ...
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(RunMojo.class);

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File targetDir;

	@Component
	private Dependencies dependencies;

	private Path emProjectHome;
	private Path emProjectModules;

	public void execute() throws MojoExecutionException {

		try {
			emProjectHome = com.commsen.em.maven.util.Constants.getHome(project);
			emProjectModules = com.commsen.em.maven.util.Constants.getModulesFolder(project);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

		/*
		 * FIXME there can be more bndrun files or the name could be different than
		 * `project.getName()`
		 */
		File runFile = new File(emProjectHome.toFile(), project.getName() + ".bndrun");
		String bndrun = getNamePart(runFile);
		File temporaryDir = new File(targetDir, "tmp/run/" + bndrun);
		File cnf = new File(temporaryDir, Workspace.CNFDIR);
		try {

			FileSetRepository fileSetRepository = new FileSetRepository(project.getName(),
					IO.tree(emProjectModules.toFile()));

			IO.mkdirs(cnf);
			try (Bndrun run = Bndrun.createBndrun(null, runFile)) {
				run.setBase(temporaryDir);
				Workspace workspace = run.getWorkspace();
				workspace.setBuildDir(cnf);
				workspace.setOffline(session.getSettings().isOffline());
				workspace.addBasicPlugin(fileSetRepository);
				for (RepositoryPlugin repo : workspace.getRepositories()) {
					repo.list(null);
				}
				run.getInfo(workspace);
				report(run);
				if (!run.isOk()) {
					return;
				}

				try {
					String runBundles = new String(IO.read(new File(targetDir, "tmp/.runBundles")),
							StandardCharsets.UTF_8);
					run.setProperty(Constants.RUNBUNDLES, runBundles);
				} finally {
					report(run);
				}

				final ProjectLauncher projectLauncher = run.getProjectLauncher();
				Runnable monitor = new Runnable() {
					@Override
					public void run() {
						while (true) {
							try {
								Thread.sleep(5 * 1000);
							} catch (InterruptedException e) {
							}
							System.out.println("update...");
							try {
								projectLauncher.update();
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				};
				
				Thread t = new Thread(monitor);
				t.start();
				projectLauncher.launch();

			}
		} catch (Exception e) {
			throw new MojoExecutionException("Something went wrong", e);
		}
	}

	private void report(Bndrun run) {
		for (String warning : run.getWarnings()) {
			logger.warn("Warning : {}", warning);
		}
		for (String error : run.getErrors()) {
			logger.error("Error   : {}", error);
		}
	}

	private String getNamePart(File runFile) {
		String nameExt = runFile.getName();
		int pos = nameExt.lastIndexOf('.');
		return (pos > 0) ? nameExt.substring(0, pos) : nameExt;
	}
}
