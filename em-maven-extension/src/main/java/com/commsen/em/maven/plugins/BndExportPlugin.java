package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.DEFAULT_TMP_BUNDLES;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_RESOLVE;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_INDEX;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_REQUIREMENTS;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_TMP_BUNDLES;
import static com.commsen.em.maven.extension.Constants.PROP_DEPLOY_TARGET;
import static com.commsen.em.maven.extension.Constants.PROP_RESOLVE_OUTPUT;
import static com.commsen.em.maven.extension.Constants.VAL_BND_VERSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ops4j.pax.swissbox.bnd.BndUtils;
import org.ops4j.pax.swissbox.bnd.OverwriteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Dependencies;
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
		createBndrunIfNotExists(project, bndrunName);
		addToPom(project, true, bndrunName);
		project.getProperties().setProperty(PROP_RESOLVE_OUTPUT, "${project.build.directory}/export/" + bndrunName);

		logger.info("Added `bnd-export-maven-plugin` to genrate list of modules needed at runtume!");
	}

	public void addToPomForExecutable(MavenProject project) throws MavenExecutionException {

		String bndrunName = getBndrunName(project);
		createBndrunIfNotExists(project, bndrunName);
		addToPom(project, false, bndrunName);

		logger.info("Added `bnd-export-maven-plugin` to genrate list of modules incuded in executable jar!");
	}

	public void addToPom(MavenProject project, boolean bundlesOnly, String bndrun) throws MavenExecutionException {
		
		Set<File> bundles = prepareDependencies(project);
		File thisArtifact = new File (project.getBuild().getDirectory(), project.getBuild().getFinalName() + "." + project.getPackaging());
		bundles.add(thisArtifact);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("bundlesOnly", bundlesOnly);
		model.put("bndrun", bndrun);
		model.put("bundles", bundles);
		
		String configuration = null;
		try {
			configuration = templates.process("META-INF/templates/bnd-export-maven-plugin-configuration.fmt", model);
		} catch (IOException | TemplateException e) {
			logger.warn("Failed to porcess template file!", e);
		}

		Plugin plugin = createPlugin("biz.aQute.bnd", "bnd-export-maven-plugin", VAL_BND_VERSION, configuration,
				"export", "export", "package");
		project.getBuild().getPlugins().add(0, plugin);
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
		addAdditionalInitialRequirments(requirements, project);
		
		Map<String, Object> model = new HashMap<>();
		model.put("requirements", requirements);
		model.put("distro", distro);
		
		String bndrunContent = null;
		try {
			bndrunContent = templates.process("META-INF/templates/bndrun.fmt", model);
		} catch (IOException | TemplateException e) {
			logger.warn("Failed to porcess template file!", e);
		}

		
		if (logger.isDebugEnabled()) logger.debug("Generated bndrun file: \n{}", bndrunContent);

		writeBndrun(bndFile, bndrunContent);
		
		if (System.getProperty("keepBndrun") != null) {
			writeBndrun(new File(project.getBasedir(), "_em.generated.bndrun"), bndrunContent);
		}
	}
	
	private void generateVariables(StringBuilder stringBuilder) throws MavenExecutionException {
		stringBuilder.append("requirements.shell = ")
				.append("osgi.identity;filter:='(osgi.identity=org.apache.felix.gogo.runtime)',")
				.append("osgi.identity;filter:='(osgi.identity=org.apache.felix.gogo.jline)',")
				.append("osgi.identity;filter:='(osgi.identity=org.apache.felix.gogo.command)'")
				.append("\n");
	}
	

	private void addAdditionalInitialRequirments(Set<String> requirements, MavenProject project) {
		String additionalInitialRequirements = project.getProperties().getProperty(PROP_CONFIG_REQUIREMENTS, "");
		if (!additionalInitialRequirements.trim().isEmpty()) {
			additionalInitialRequirements = additionalInitialRequirements.replace(System.getProperty("line.separator"), "");
		}
		
		for (String string : additionalInitialRequirements.split(",")) {
			requirements.add(string);
		}
	}

	private void writeBndrun(File generatedBndrunFile, String content) throws MavenExecutionException {
		try {
			generatedBndrunFile.createNewFile();
			PrintWriter writer = new PrintWriter(generatedBndrunFile, "UTF-8");
			writer.print(content.replaceAll("\\t", "    "));
			writer.close();
		} catch (IOException e) {
			throw new MavenExecutionException("Failed to create temprary bndrun file!", e);
		}
	}


	private Set<File> prepareDependencies(MavenProject project) {

		logger.debug("Analysing project's BOMs!");

		File tmpBundleFolder = new File(project.getBasedir(), project.getProperties().getProperty(PROP_CONFIG_TMP_BUNDLES, DEFAULT_TMP_BUNDLES));
		try {
			FileUtils.forceMkdir(tmpBundleFolder);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create temprary directory: " + tmpBundleFolder, e);
		}
		filesToCleanup.add(tmpBundleFolder);

		Set<File> bundleSet = new HashSet<File>();

		boolean indexBundles = project.getProperties().containsKey(PROP_CONFIG_INDEX);
		
		/*
		 *  get resolved artifacts
		 */
		Set<Artifact> resolvedArtifacts = null;
		try {
			resolvedArtifacts = dependencies.asArtifacts(project);
		} catch (MavenExecutionException e) {
			throw new RuntimeException("Failed to analyze dependencies", e);
		}

		/*
		 * For each resolved artifact :
		 *  - convert to bundle if it's not
		 *  - add it to the bundle set 
		 */
		resolvedArtifacts.stream().forEach(a -> addToBundleSet(a, tmpBundleFolder, bundleSet, indexBundles));
		
		/*
		 * get managed artifacts
		 * TODO: get only from special imports (for example those with 'index' type or classifier)
		 */
		Set<Artifact> managedArtifacts = null;
		try {
			managedArtifacts = dependencies.mangedAsArtifacts(project);
		} catch (MavenExecutionException e) {
			throw new RuntimeException("Failed to analyze dependencies", e);
		}
		
		/*
		 * For each managed artifact :
		 *  - convert to bundle if it's not
		 *  - add it to the bundle set 
		 */
		managedArtifacts.stream().forEach(a -> addToBundleSet(a, tmpBundleFolder, bundleSet, indexBundles));
	
		return bundleSet;

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
					FileUtils.copyFile(artifact.getFile(), new File (bundlesDirectory, f.getName()));
				} catch (IOException e) {
					throw new RuntimeException("Failed to copy file to temporary folder", e);
				}
			}
		} else {
			f = new File(bundlesDirectory, artifact.getArtifactId() + "-" + artifact.getVersion() +"-EM.jar");
			makeBundle(artifact, f);
		}
		
		bundlesSet.add(f);
		logger.debug("Made '{}' module available to the resolver", artifact);
	}

	private boolean makeBundle(Artifact artifact, File targetFile) {
		Properties properties = new Properties();
		properties.put("Bundle-SymbolicName", artifact.getArtifactId());
		properties.put("Bundle-Version", Version.semantic(artifact.getVersion()));
		properties.put("Original-Version", artifact.getVersion());

		try (
			FileInputStream fileStream = new FileInputStream(artifact.getFile());
			InputStream bndStream = BndUtils.createBundle(fileStream, properties, artifact.getFile().getName(), OverwriteMode.FULL);
		    OutputStream outStream = new FileOutputStream(targetFile);
		) {
			
		    byte[] buffer = new byte[8 * 1024];
		    int bytesRead;
		    while ((bytesRead = bndStream.read(buffer)) != -1) {
		        outStream.write(buffer, 0, bytesRead);
		    }
		    
			logger.debug("'{}' wrapped in module '{}'", artifact.getFile(), targetFile);
		    return true;

		} catch (IOException e) {
			logger.warn("Failed to convert '{}' to module ", artifact, e);
		    return false;
		}
	}
	
}
