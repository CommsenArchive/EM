package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.DEFAULT_TMP_BUNDLES;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_RESOLVE;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_INDEX;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_TMP_BUNDLES;
import static com.commsen.em.maven.extension.Constants.PROP_CONTRACTORS;
import static com.commsen.em.maven.extension.Constants.PROP_CONTRACTS;
import static com.commsen.em.maven.extension.Constants.PROP_DEPLOY_TARGET;
import static com.commsen.em.maven.extension.Constants.PROP_RESOLVE_OUTPUT;
import static com.commsen.em.maven.extension.Constants.PROP_RUN_PROPERTIES;
import static com.commsen.em.maven.extension.Constants.VAL_EM_VERSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ops4j.pax.swissbox.bnd.BndUtils;
import org.ops4j.pax.swissbox.bnd.OverwriteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.extension.Constants;
import com.commsen.em.maven.util.Dependencies;
import com.commsen.em.maven.util.Flag;
import com.commsen.em.maven.util.Templates;
import com.commsen.em.maven.util.Version;

import freemarker.template.TemplateException;

@Component(role = BndExportPlugin.class)
public class BndExportPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(BndExportPlugin.class);

	@Requirement
	private Dependencies dependencies;

	@Requirement
	private Templates templates;

	private List<File> filesToCleanup = new LinkedList<>();

	public void addToPomForExport(MavenProject project) throws MavenExecutionException {
		String bndrunName = getBndrunName(project);
		addToPom(project, true, bndrunName);
		project.getProperties().setProperty(PROP_RESOLVE_OUTPUT, "${project.build.directory}/export/" + bndrunName);
		logger.info("Added `bnd-export-maven-plugin` to genrate list of modules needed at runtume!");
	}

	public void addToPomForExecutable(MavenProject project) throws MavenExecutionException {
		addToPom(project, false, getBndrunName(project));
		logger.info("Added `bnd-export-maven-plugin` to genrate list of modules incuded in executable jar!");
	}

	public void addToPom(MavenProject project, boolean bundlesOnly, String bndrun) throws MavenExecutionException {

		Set<File> bundles = prepareDependencies(project);

		File thisArtifact = new File(project.getBuild().getDirectory(),
				project.getBuild().getFinalName() + "." + project.getPackaging());
		bundles.add(thisArtifact);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("bundlesOnly", bundlesOnly);
		model.put("bndrun", bndrun);
		model.put("bundles", bundles);

		String configuration = null;
		try {
			configuration = templates.process("META-INF/templates/bnd-export-maven-plugin-configuration.fmt", model);
		} catch (IOException | TemplateException e) {
			throw new MavenExecutionException("Failed to process template file!", e);
		}

		Plugin plugin = createPlugin("com.commsen.em", "em-maven-plugin", VAL_EM_VERSION, configuration, "export", "export", "package");
		
//		Plugin plugin = createPlugin("biz.aQute.bnd", "bnd-export-maven-plugin", VAL_BND_VERSION, configuration,
//				"export", "export", "package");
		project.getBuild().getPlugins().add(0, plugin);

		createBndrunIfNotExists(project, bndrun);

	}

	public void cleanup() {
		for (File file : filesToCleanup) {
			FileUtils.deleteQuietly(file);
		}
	}

	private String getBndrunName(MavenProject project) throws MavenExecutionException {
		String bndrunName = project.getProperties().getProperty(PROP_ACTION_RESOLVE + ".bndrun", "");
		if (bndrunName.trim().isEmpty()) {
			bndrunName = project.getName();
		}

		if (bndrunName.endsWith(".bndrun")) {
			bndrunName = bndrunName.substring(0, bndrunName.length() - ".bndrun".length());
		}

		int count = 0;
		while (new File(project.getBasedir(), bndrunName + ".bndrun").isFile()) {
			bndrunName = bndrunName + "." + ++count;
		}

		return bndrunName;
	}

	private void createBndrunIfNotExists(MavenProject project, String bndrunName) throws MavenExecutionException {

		if (bndrunName == null || bndrunName.trim().isEmpty()) {
			throw new MavenExecutionException("Invalid bndrun name: " + bndrunName, project.getFile());
		}

		File bndrunFile = new File(project.getBasedir(), bndrunName + ".bndrun");

		if (bndrunFile.exists() && !bndrunFile.isFile()) {
			throw new MavenExecutionException("'" + bndrunFile + "' is not a file!", project.getFile());
		} else {
			String distro = project.getProperties().getProperty(PROP_DEPLOY_TARGET, "");
			generateBndrun(project, distro, bndrunFile);
			filesToCleanup.add(bndrunFile);
		}
	}

	private void generateBndrun(MavenProject project, String distro, File bndFile) throws MavenExecutionException {

		Set<String> requirements = new HashSet<>();
		requirements.add("osgi.identity;filter:='(osgi.identity=" + project.getArtifactId() + ")'");

		String contracts = project.getProperties().getProperty(PROP_CONTRACTS);
		if (contracts != null && !contracts.trim().isEmpty()) {
			String[] contractsArray = contracts.split("[\\s]*,[\\s]*");
			for (String contract : contractsArray) {
				requirements.add("em.contract;filter:='(em.contract=" + contract + ")';effective:=assemble");
			}
		}

		Set<String> runProperties = new HashSet<>();
		
		String runPropertiesText = project.getProperties().getProperty(PROP_RUN_PROPERTIES);
		if (runPropertiesText != null && !runPropertiesText.trim().isEmpty()) {
			String[] propertiesArray = runPropertiesText.split("[\\s]*\\n[\\s]*");
			runProperties.addAll(Arrays.asList(propertiesArray));
		}
		
		
		Map<String, Object> model = new HashMap<>();
		model.put("requirements", requirements);
		model.put("runProperties", runProperties);
		model.put("distro", distro);

		String bndrunContent = null;
		try {
			bndrunContent = templates.process("META-INF/templates/bndrun.fmt", model);
		} catch (IOException | TemplateException e) {
			throw new MavenExecutionException("Failed to process template file!", e);
		}

		if (logger.isDebugEnabled())
			logger.debug("Generated bndrun file: \n{}", bndrunContent);

		writeBndrun(bndFile, bndrunContent);

		if (Flag.keepBndrun()) {
			writeBndrun(new File(project.getBasedir(), "_em.generated.bndrun"), bndrunContent);
		}
	}

	private void writeBndrun(File generatedBndrunFile, String content) throws MavenExecutionException {

		try {
			generatedBndrunFile.createNewFile();
			generatedBndrunFile.deleteOnExit();
			PrintWriter writer = new PrintWriter(generatedBndrunFile, "UTF-8");
			writer.print(content.replaceAll("\\t", "    "));
			writer.close();
		} catch (IOException e) {
			throw new MavenExecutionException("Failed to create temprary bndrun file!", e);
		}
	}

	private Set<File> prepareDependencies(MavenProject project) {

		if (logger.isDebugEnabled()) {
			logger.debug("Analysing project's BOMs!");
		} else if (Flag.verbose()) {
			logger.info("Analysing project's BOMs!");
		}

		File tmpBundleFolder = new File(project.getBasedir(),
				project.getProperties().getProperty(PROP_CONFIG_TMP_BUNDLES, DEFAULT_TMP_BUNDLES));
		try {
			FileUtils.forceMkdir(tmpBundleFolder);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create temprary directory: " + tmpBundleFolder, e);
		}
		filesToCleanup.add(tmpBundleFolder);

		Set<File> bundleSet = new HashSet<File>();

		boolean generateIndex = project.getProperties().containsKey(PROP_CONFIG_INDEX);

		processModules(project);

		Set<Artifact> artifacts = new HashSet<>();
		try {
			artifacts.addAll(dependencies.asArtifacts(project));
			artifacts.addAll(dependencies.managedAsArtifacts(project));
		} catch (MavenExecutionException e) {
			throw new RuntimeException("Failed to analyze dependencies", e);
		}

		/*
		 * For each artifact : - convert to bundle if it's not - add it to the bundle
		 * set
		 */
		artifacts.stream().forEach(a -> addToBundleSet(a, tmpBundleFolder, bundleSet, generateIndex));

		return bundleSet;

	}

	public void processModules(MavenProject project) {
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
		modulesSet.add("com.commsen.em.contractors:em.contractors.runtime:" + VAL_EM_VERSION);
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
				Artifact pomArtifact = dependencies.asArtifact(project, dependency);
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
			f = artifact.getFile();
			/*
			 * If index is to be created copy the bundles to the temporary folder
			 */
			if (indexGeneration) {
				try {
					FileUtils.copyFile(artifact.getFile(), new File(bundlesDirectory, f.getName()));
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
			logger.debug("Made '{}' module available to the resolver", artifact);
		} else if (Flag.verbose()) {
			logger.info("Made '{}' module available to the resolver", artifact);
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
