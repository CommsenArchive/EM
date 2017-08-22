package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.DEFAULT_TMP_BUNDLES;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_RESOLVE;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_REQUIREMENTS;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_TMP_BUNDLES;
import static com.commsen.em.maven.extension.Constants.PROP_DEPLOY_OUTPUT;
import static com.commsen.em.maven.extension.Constants.PROP_RESOLVE_OUTPUT;
import static com.commsen.em.maven.extension.Constants.VAL_BND_VERSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

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

import com.commsen.em.maven.extension.Constants;
import com.commsen.em.maven.util.DependencyUtil;
import com.commsen.em.maven.util.VersionUtil;

@Component(role = BndExportPlugin.class)
public class BndExportPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(BndExportPlugin.class);

	@Requirement
	private DependencyUtil dependencyUtil;

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
		
		StringBuilder configuration = new StringBuilder()
			.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") //
			.append("<configuration>") //
			.append("	<failOnChanges>false</failOnChanges>") //
			.append("	<resolve>true</resolve>") //
			.append("	<bundlesOnly>").append(bundlesOnly).append("</bundlesOnly>") //
			.append("	<bndruns>") //
			.append("		<bndrun>").append(bndrun).append(".bndrun</bndrun>") //
			.append("	</bndruns>") //
			.append("	<useMavenDependencies>false</useMavenDependencies>") //
			.append("	<bundles>") //
			.append("		<bundle>").append(thisArtifact).append("</bundle>");
		
		for (File file : bundles) {
			configuration.append("<bundle>").append(file).append("</bundle>");
		}

		configuration
			.append("</bundles>") //
			.append("</configuration>"); //

		Plugin plugin = createPlugin("biz.aQute.bnd", "bnd-export-maven-plugin", VAL_BND_VERSION, configuration.toString(),
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

		List<URI> indexesFromDependencies = dependencyUtil.getIndexesFromDependencies(project);
		List<URI> indexesFromProperties = getIndexesFromProperties(project);

		List<URI> indexes = new LinkedList<>();
		indexes.addAll(indexesFromDependencies);
		indexes.addAll(indexesFromProperties);

		if (bndrunFile.exists()) {
			if (bndrunFile.isFile()) {
				if (!indexesFromDependencies.isEmpty()) {
					logger.warn(
							"Custom bndrun file is provided! The following indexes found in repositories will be ignored: "
									+ indexesFromDependencies);
				}
				if (!indexesFromProperties.isEmpty()) {
					logger.warn(
							"Custom bndrun file is provided! The following indexes found in properties will be ignored: "
									+ indexesFromProperties);
				}
			} else {
				throw new MavenExecutionException("{} is not a file!", project.getFile());
			}
		} else {
			
			String distro = project.getProperties().getProperty(PROP_DEPLOY_OUTPUT, "");
			if (distro.trim().isEmpty()) {
				generateBndrun(project, bndrunFile, indexes);
			} else {
				generateBndrun4Distro(project, distro, bndrunFile, indexes);
			}
			filesToCleanup.add(bndrunFile);
			
		}
	}

	private void generateBndrun(MavenProject project, File bndFile, List<URI> indexes) throws MavenExecutionException {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("-standalone:\n");
		stringBuilder.append("-runee: JavaSE-1.8\n");
		stringBuilder.append("-runfw: org.apache.felix.framework;version='[5,6)'\n");
//		stringBuilder.append("-runfw: org.eclipse.osgi;version='[3.10,4)'\\n");

		/*
		 * The following line enables console in Equinox!
		 * However it also breaks the resolver (it resolves OK but output does not contain required bundles) !!!
		 */
//		stringBuilder.append("-runproperties: osgi.console=, osgi.console.enable.builtin=false \\\n");

		stringBuilder.append("-resolve.effective: resolve, active, assemble\n");
		stringBuilder.append("-runrequires: \\\n");
		stringBuilder.append(getAdditionalInitialRequirments(project));
		stringBuilder.append("		osgi.identity;filter:='(osgi.identity=");
		stringBuilder.append(project.getArtifactId());
		stringBuilder.append(")'");
		String bndrunContent = stringBuilder.toString();

		
		logger.debug("Generated bndrun file: \n{}", bndrunContent);

		
		writeBndrun(bndFile, bndrunContent);
		
		if (System.getProperty("keepBndrun") != null) {
			writeBndrun(new File(project.getBasedir(), "_em.generated.bndrun"), bndrunContent);
		}

	}
	
	private void generateBndrun4Distro(MavenProject project, String distro, File bndFile, List<URI> indexes) throws MavenExecutionException {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("-standalone:\n");
		stringBuilder.append("-distro: ").append(distro).append(";version=file \n");
		stringBuilder.append("-resolve.effective: resolve, active, assemble\n");
		stringBuilder.append("-runrequires: \\\n");
		stringBuilder.append(		getAdditionalInitialRequirments(project));
		stringBuilder.append("		osgi.identity;filter:='(osgi.identity=").append(project.getArtifactId()).append(")'");
		String bndrunContent = stringBuilder.toString();

		logger.debug("Generated bndrun file: \n{}", bndrunContent);

		writeBndrun(bndFile, bndrunContent);

		if (System.getProperty("keepBndrun") != null) {
			writeBndrun(new File(project.getBasedir(), "_em.generated.bndrun"), bndrunContent);
		}
}

	private String getAdditionalInitialRequirments(MavenProject project) {
		String additionalInitialRequirements = project.getProperties().getProperty(PROP_CONFIG_REQUIREMENTS, "");
		
		if (!additionalInitialRequirements.trim().isEmpty()) {
			additionalInitialRequirements = additionalInitialRequirements.replace(System.getProperty("line.separator"), "\\\n");
			additionalInitialRequirements += ",\\\n";
		}
		return additionalInitialRequirements;
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

	private List<URI> getIndexesFromProperties(MavenProject project) throws MavenExecutionException {
		List<URI> indexes = new LinkedList<>();
		String indexesURIs = project.getProperties().getProperty(PROP_ACTION_RESOLVE + ".indexes", "");
		if (!indexesURIs.trim().isEmpty()) {
			indexes = Arrays.stream(indexesURIs.split(",")).map(s -> URI.create(s)).collect(Collectors.toList());
		}
		return indexes;
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

		boolean indexBundles = project.getProperties().containsKey(Constants.PROP_CONFIG_INDEX);
		
		/*
		 *  get resolved artifacts
		 */
		Set<Artifact> resolvedArtifacts = null;
		try {
			resolvedArtifacts = dependencyUtil.getProjectDependencies(project);
		} catch (MavenExecutionException e) {
			throw new RuntimeException("Failed to analyze dependencies", e);
		}


		/*
		 * For each resolved artifact :
		 *  - convert to bundle if it's not
		 *  - add it to the bundle set 
		 */
		resolvedArtifacts.stream()
			.forEach(a -> {
				File f;
				if (dependencyUtil.isOSGiBundle(a)) {
					f = a.getFile();
					/*
					 * If index is to be created copy the bundles to the temporary folder
					 */
					if (indexBundles) {
						try {
							FileUtils.copyFile(a.getFile(), new File (tmpBundleFolder, f.getName()));
						} catch (IOException e) {
							throw new RuntimeException("Failed to copy file to temporary folder", e);
						}
					}
				} else {
					f = new File(tmpBundleFolder, a.getArtifactId() + "-" + a.getVersion() +"-EM.jar");
					makeBundle(a, f);
				}
				
				bundleSet.add(f);
				logger.debug("Made '{}' module available to the resolver", a);
				
			});
		
		/*
		 * get managed artifacts
		 * TODO: get only from special imports (for example those with 'index' type or classifier)
		 */
		Set<Artifact> managedArtifacts = null;
		try {
			managedArtifacts = dependencyUtil.getProjectMangedDependencies(project);
		} catch (MavenExecutionException e) {
			throw new RuntimeException("Failed to analyze dependencies", e);
		}
				
		
		/*
		 * For each managed artifact :
		 *  - convert to bundle if it's not
		 *  - add it to the bundle set 
		 */
		managedArtifacts.stream()
			.forEach(a -> {
				File f; 
				if (dependencyUtil.isOSGiBundle(a)) {
					f = a.getFile();
					/*
					 * If index is to be created copy the bundles to the temporary folder
					 */
					if (indexBundles) {
						try {
							FileUtils.copyFile(a.getFile(), new File (tmpBundleFolder, f.getName()));
						} catch (IOException e) {
							throw new RuntimeException("Failed to copy file to temporary folder", e);
						}
					}
				} else {
					f = new File(tmpBundleFolder, a.getArtifactId() + "-" + a.getVersion() +"-EM.jar");
					makeBundle(a, f);
				}
				
				bundleSet.add(f);
				logger.debug("Made '{}' module available to the resolver", a);
			});

		//debug
//		bundleSet.stream().forEach(a -> System.out.println(" ===== Bundle : " + a));
//		Arrays.stream(tmpBundleFolder.listFiles()).forEach(f -> System.out.println(" ===== File : " + f));
		
		return bundleSet;

	}

	private boolean makeBundle(Artifact artifact, File targetFile) {
		Properties properties = new Properties();
		properties.put("Bundle-SymbolicName", artifact.getArtifactId());
		properties.put("Bundle-Version", VersionUtil.sementicVersion(artifact.getVersion()));
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
