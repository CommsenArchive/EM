package org.em.maven.plugin;

import static com.commsen.em.maven.util.Constants.PROP_CONFIG_INDEX;
import static com.commsen.em.maven.util.Constants.PROP_CONTRACTORS;
import static com.commsen.em.maven.util.Constants.VAL_EXTENSION_VERSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.ops4j.pax.swissbox.bnd.BndUtils;
import org.ops4j.pax.swissbox.bnd.OverwriteMode;
import org.osgi.resource.Requirement;
import org.osgi.service.resolver.ResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Dependencies;
import com.commsen.em.maven.util.Flag;
import com.commsen.em.maven.util.Version;
import com.commsen.em.storage.ContractStorage;
import com.commsen.em.storage.PathsStorage;

import aQute.bnd.build.Container;
import aQute.bnd.build.Workspace;
import aQute.bnd.osgi.Constants;
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

	@Component
	private Dependencies dependencies;
	
	@Component
	private ContractStorage contractStorage;

	@Component
	private PathsStorage pathsStorage;
	
	private Path emProjectHome;
	private Path emProjectModules;
	private Path emProjectGeneratedModules;

	public void execute() throws MojoExecutionException {
		try {

			emProjectHome = com.commsen.em.maven.util.Constants.getHome(project);
			emProjectModules = com.commsen.em.maven.util.Constants.getModulesFolder(project);
			emProjectGeneratedModules = com.commsen.em.maven.util.Constants.getGeneratedModulesFolder(project);
			
			Set<File> myBundles = prepareDependencies(session.getProjectBuildingRequest(), project);
			File thisArtifact = new File(project.getBuild().getDirectory(),
					project.getBuild().getFinalName() + "." + project.getPackaging());
			myBundles.add(thisArtifact);
			
			FileSetRepository fileSetRepository = new FileSetRepository(project.getName(), myBundles);

			for (File runFile : bndruns) {
				Path runFileCopy = emProjectHome.resolve(runFile.getName());
				Files.copy(runFile.toPath(), runFileCopy, StandardCopyOption.REPLACE_EXISTING);
				export(runFileCopy.toFile(), fileSetRepository);
			}
		} catch (Exception e) {

			Throwable t = e;
			ResolutionException rex = null;
			do {
				System.out.println("exception: " + t.getClass());
				if (t.getClass().isAssignableFrom(ResolutionException.class)) {
					rex = (ResolutionException)t;
				}
				t = t.getCause();
			} while (t != null);

			if (rex != null) {
				System.out.println("ResolutionException:" + rex);
	
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
			} else {
				throw new MojoExecutionException(e.getMessage(), e);
			}

		} finally {
			try {
				pathsStorage.close();
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
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
					String runBundles = run.resolve(failOnChanges, true);
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
					run.exportRunbundles(null, com.commsen.em.maven.util.Constants.getExportedModulesFolder(project).toFile());
				} else {
					File executableJar = new File(targetDir, bndrun + ".jar");
					run.export(null, false, executableJar);
					
					Set<Path> existingModules = Files.list(emProjectModules).collect(Collectors.toSet());
					
					for (Container container : run.getRunbundles()) {
						createSymlink(existingModules, container);
					}
					for (Container container : run.getRunFw()) {
						createSymlink(existingModules, container);
					}					
					for (Path path : existingModules) {
						Files.delete(path);
					}
				}
			} finally {
				report(run);
			}
		}
	}

	/**
	 * @param existingModules
	 * @param container
	 * @throws IOException
	 */
	private void createSymlink(Set<Path> existingModules, Container container) throws IOException {
		Path file = pathsStorage.getEmPath(container.getFile().toPath());
		Path link = emProjectModules.resolve(file.getFileName());
		if (Files.isSymbolicLink(link) || Files.exists(link)) {
			Files.delete(link);
		}
		Files.createSymbolicLink(link, file);
		existingModules.remove(link);
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

	private Set<File> prepareDependencies(ProjectBuildingRequest projectBuildingRequest, MavenProject project) {

		if (logger.isDebugEnabled()) {
			logger.debug("Analysing project's BOMs!");
		} else if (Flag.verbose()) {
			logger.info("Analysing project's BOMs!");
		}

		Set<File> bundleSet = new HashSet<File>();

		boolean generateIndex = project.getProperties().containsKey(PROP_CONFIG_INDEX);

		processModules(projectBuildingRequest, project);

		Set<Artifact> artifacts = new HashSet<>();
		try {
			artifacts.addAll(dependencies.asArtifacts(projectBuildingRequest, project));
			artifacts.addAll(dependencies.managedAsArtifacts(projectBuildingRequest, project));
		} catch (MavenExecutionException e) {
			throw new RuntimeException("Failed to analyze dependencies", e);
		}

		/*
		 * For each artifact : - convert to bundle if it's not - add it to the bundle
		 * set
		 */
		artifacts.stream().forEach(a -> addToBundleSet(a, emProjectGeneratedModules.toFile(), bundleSet, generateIndex));

		return bundleSet;

	}

	public void processModules(ProjectBuildingRequest projectBuildingRequest, MavenProject project) {
		String modulesText = project.getProperties().getProperty(PROP_CONTRACTORS);
		if (modulesText == null || modulesText.trim().isEmpty()) {
			if (Flag.verbose()) {
				logger.info("No available modules in '{}'", PROP_CONTRACTORS);
			} else if (logger.isDebugEnabled()) {
				logger.debug("No available modules in '{}'", PROP_CONTRACTORS);
			}
			return;
		}
		String[] modulesArray = modulesText.split("[\\s]*[,\\n][\\s]*");
		Set<String> modulesSet = Arrays.stream(modulesArray).collect(Collectors.toSet());
		modulesSet.add("com.commsen.em.contractors:em.contractors.runtime:" + VAL_EXTENSION_VERSION);
		for (String moduleText : modulesSet) {
			String[] coordinates = moduleText.split(":");
			if (coordinates.length != 3) {
				logger.warn("Invalid maven coordinates for module '{}'! It will be ignored!", moduleText);
				continue;
			}

			Dependency dependency = new Dependency();
			dependency.setGroupId(coordinates[0]);
			dependency.setArtifactId(coordinates[1]);
			dependency.setVersion(coordinates[2]);
			dependency.setScope("runtime");
			dependency.setType("pom");

			try {
				Artifact pomArtifact = dependencies.asArtifact(projectBuildingRequest, dependency);
				dependency.setType("jar");
				MavenXpp3Reader reader = new MavenXpp3Reader();
				Model model = reader.read(new FileInputStream(pomArtifact.getFile()));
				DependencyManagement dm = model.getDependencyManagement();

				if (dm == null) {
					dependencies.addToDependencyManagement(project, dependency);
				} else {
					for (Dependency d : dm.getDependencies()) {
						/*
						 * TODO handle variables properly! For now assume variable is referring to the
						 * contract's artifact itself (that's what EM contractors do).
						 */
						if (d.getArtifactId().startsWith("${")) {
							dependencies.addToDependencyManagement(project, dependency);
						} else {
							dependencies.addToDependencyManagement(project, d);
						}
					}
				}

			} catch (Exception e) {
				logger.warn("Could not process modules from " + coordinates[0] + ":" + coordinates[1] + ":"
						+ coordinates[2], e);
			}
		}

	}

	/**
	 * @param artifact
	 * @param bundlesDirectory
	 * @param bundlesSet
	 * @param indexGeneration
	 */
	private void addToBundleSet(Artifact artifact, File bundlesDirectory, Set<File> bundlesSet,
			boolean indexGeneration) {
		File f;
		if (dependencies.isOSGiBundle(artifact)) {
			f = pathsStorage.getEmPath(artifact.getFile().toPath()).toFile();
			/*
			 * If index is to be created copy the bundles to the temporary folder
			 */
			if (indexGeneration) {
				try {
					FileUtils.copyFile(f, new File(bundlesDirectory, f.getName()));
				} catch (IOException e) {
					throw new RuntimeException("Failed to copy file to temporary folder", e);
				}
			}
		} else {
			f = new File(bundlesDirectory, artifact.getArtifactId() + "-" + artifact.getVersion() + "-EM.jar");
			makeBundle(artifact, f);
		}

		bundlesSet.add(f);
		if (logger.isDebugEnabled()) {
			logger.debug("Resolver input enty: module '{}' from {}", artifact, f);
		} else if (Flag.verbose()) {
			logger.info("Resolver input enty: module '{}' from {}", artifact, f);
		}
	}

	private boolean makeBundle(Artifact artifact, File targetFile) {
		Properties properties = new Properties();
		properties.put("Bundle-SymbolicName", artifact.getArtifactId());
		properties.put("Bundle-Version", Version.semantic(artifact.getVersion()));
		properties.put("Original-Version", artifact.getVersion());

		try (FileInputStream fileStream = new FileInputStream(artifact.getFile());
				InputStream bndStream = BndUtils.createBundle(fileStream, properties, artifact.getFile().getName(),
						OverwriteMode.FULL);
				OutputStream outStream = new FileOutputStream(targetFile);) {

			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			while ((bytesRead = bndStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("'{}' wrapped in module '{}'", artifact.getFile(), targetFile);
			} else if (Flag.verbose()) {
				logger.info("'{}' wrapped in module '{}'", artifact.getFile(), targetFile);
			}
			return true;

		} catch (IOException e) {
			logger.warn("Failed to convert '{}' to module ", artifact, e);
			return false;
		}
	}
}
