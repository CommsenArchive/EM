package org.em.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.service.resolver.ResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.contract.storage.ContractStorage;
import com.commsen.em.contract.storage.NitriteContractStorage;

import aQute.bnd.build.Workspace;
import aQute.bnd.maven.lib.resolve.DependencyResolver;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.resource.FilterParser;
import aQute.bnd.osgi.resource.FilterParser.Expression;
import aQute.bnd.repository.fileset.FileSetRepository;
import aQute.bnd.service.RepositoryPlugin;
import aQute.lib.io.IO;
import biz.aQute.resolve.Bndrun;
import biz.aQute.resolve.ResolveProcess;

/**
 * Goal which ...
 */
@Mojo(name = "export", defaultPhase = LifecyclePhase.PACKAGE)
public class ExportMojo extends aQute.bnd.maven.export.plugin.ExportMojo {

	private static final Logger logger = LoggerFactory.getLogger(ExportMojo.class);

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	MavenProject project;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	private RepositorySystemSession repositorySession;

	@Parameter(readonly = true, required = true)
	private List<File> bndruns;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File targetDir;

	@Parameter(readonly = true, required = false)
	private List<File> bundles;

	@Parameter(defaultValue = "true")
	private boolean useMavenDependencies;

	@Parameter(defaultValue = "false")
	private boolean resolve;

	@Parameter(defaultValue = "true")
	private boolean failOnChanges;

	@Parameter(defaultValue = "false")
	private boolean bundlesOnly;

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;

	private int errors = 0;

	@Component
	private RepositorySystem system;

	@Component
	private ProjectDependenciesResolver resolver;

	public void execute() throws MojoExecutionException {
		try {
			DependencyResolver dependencyResolver = new DependencyResolver(project, repositorySession, resolver,
					system);

			FileSetRepository fileSetRepository = dependencyResolver.getFileSetRepository(project.getName(), bundles,
					useMavenDependencies);

			for (File runFile : bndruns) {
				export(runFile, fileSetRepository);
			}
		} catch (Exception e) {

			Throwable t = e;

			do {
				System.out.println("exception: " + t.getClass());
				if (t.getClass().isAssignableFrom(ResolutionException.class))
					break;
				t = t.getCause();
			} while (t.getCause() != null);

			ResolutionException rex = (ResolutionException) t;
			System.out.println("ResolutionException:" + rex);

			ContractStorage contractStorage = null;
			try {
				contractStorage = new NitriteContractStorage();
			} catch (IOException e1) {
				throw new MojoExecutionException(e.getMessage(), e);
			}

			UnsatisfiedRequirementsException.Builder exBuilder = new UnsatisfiedRequirementsException.Builder();
			for (Requirement requirement : rex.getUnresolvedRequirements()) {
				if (rex.getMessage().contains(requirement.toString())) {
					Set<String> found = contractStorage.getContractors(requirement);
					if (found != null && !found.isEmpty()) {
						found.forEach(contractor -> exBuilder.add(requirement.toString(), contractor));
					} else {
						exBuilder.add(requirement.toString());
					}
				}
			}
			UnsatisfiedRequirementsException ure = exBuilder.build();
			throw new MojoExecutionException(ure.getMessage(), ure);

		}

		if (errors > 0)
			throw new MojoExecutionException(errors + " errors found");
	}

	private void export(File runFile, FileSetRepository fileSetRepository) throws Exception {
		if (!runFile.exists()) {
			logger.error("Could not find bnd run file {}", runFile);
			errors++;
			return;
		}
		String bndrun = getNamePart(runFile);
		File temporaryDir = new File(targetDir, "tmp/export/" + bndrun);
		File cnf = new File(temporaryDir, Workspace.CNFDIR);
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
			if (resolve) {
				try {
					String runBundles = run.resolve(failOnChanges, false);
					if (!run.isOk()) {
						return;
					}
					run.setProperty(Constants.RUNBUNDLES, runBundles);
				} catch (ResolutionException re) {
					logger.error("Unresolved requirements: {}", ResolveProcess.format(re.getUnresolvedRequirements()));
					throw re;
				} finally {
					report(run);
				}
			}
			try {
				if (bundlesOnly) {
					File runbundlesDir = new File(targetDir, "export/" + bndrun);
					IO.mkdirs(runbundlesDir);
					run.exportRunbundles(null, runbundlesDir);
				} else {
					File executableJar = new File(targetDir, bndrun + ".jar");
					run.export(null, false, executableJar);
				}
			} finally {
				report(run);
			}
		}
	}

	private String getNamePart(File runFile) {
		String nameExt = runFile.getName();
		int pos = nameExt.lastIndexOf('.');
		return (pos > 0) ? nameExt.substring(0, pos) : nameExt;
	}

	private void report(Bndrun run) {
		for (String warning : run.getWarnings()) {
			logger.warn("Warning : {}", warning);
		}
		for (String error : run.getErrors()) {
			logger.error("Error   : {}", error);
			errors++;
		}
	}
}
