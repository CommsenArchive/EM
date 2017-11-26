package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.util.Constants.INTERNAL_DISTRO_FILE;
import static com.commsen.em.maven.util.Constants.PROP_ACTION_RESOLVE;
import static com.commsen.em.maven.util.Constants.PROP_CONTRACTS;
import static com.commsen.em.maven.util.Constants.PROP_EXECUTABLE_RUN_PROPERTIES;
import static com.commsen.em.maven.util.Constants.PROP_RESOLVE_OUTPUT;
import static com.commsen.em.maven.util.Constants.VAL_EXTENSION_VERSION;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Dependencies;
import com.commsen.em.maven.util.Flag;
import com.commsen.em.maven.util.Templates;

import freemarker.template.TemplateException;

@Component(role = BndExportPlugin.class)
public class BndExportPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(BndExportPlugin.class);

	@Requirement
	private Dependencies dependencies;

	@Requirement
	private Templates templates;

	private List<File> filesToCleanup = new LinkedList<>();

	public void addToPomForExport(ProjectBuildingRequest projectBuildingRequest, MavenProject project) throws MavenExecutionException {
		String bndrunName = getBndrunName(project);
		addToPom(projectBuildingRequest, project, true, bndrunName);
		project.getProperties().setProperty(PROP_RESOLVE_OUTPUT, "${project.build.directory}/export/" + bndrunName);
		logger.info("Added `bnd-export-maven-plugin` to genrate list of modules needed at runtume!");
	}

	public void addToPomForExecutable(ProjectBuildingRequest projectBuildingRequest, MavenProject project) throws MavenExecutionException {
		addToPom(projectBuildingRequest, project, false, getBndrunName(project));
		logger.info("Added `bnd-export-maven-plugin` to genrate list of modules incuded in executable jar!");
	}

	public void addToPom(ProjectBuildingRequest projectBuildingRequest, MavenProject project, boolean bundlesOnly, String bndrun) throws MavenExecutionException {

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("bundlesOnly", bundlesOnly);
		model.put("bndrun", bndrun);
		model.put("bundles", Collections.EMPTY_LIST);

		String configuration = null;
		try {
			configuration = templates.process("META-INF/templates/bnd-export-maven-plugin-configuration.fmt", model);
		} catch (IOException | TemplateException e) {
			throw new MavenExecutionException("Failed to process template file!", e);
		}

		Plugin plugin = createPlugin("com.commsen.em", "em-maven-plugin", VAL_EXTENSION_VERSION, configuration, "export", "export", "package");
		plugin.setExtensions(true);
		
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
			String distro = project.getProperties().getProperty(INTERNAL_DISTRO_FILE, "");
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
		
		String runPropertiesText = project.getProperties().getProperty(PROP_EXECUTABLE_RUN_PROPERTIES);
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
			writeBndrun(new File(project.getBasedir(), "_em.generated.bndrun"), bndrunContent, false);
		}
	}

	private void writeBndrun(File generatedBndrunFile, String content) throws MavenExecutionException {
		writeBndrun(generatedBndrunFile, content, true);
	}

	private void writeBndrun(File generatedBndrunFile, String content, boolean deleteOnExit) throws MavenExecutionException {

		try {
			generatedBndrunFile.createNewFile();
			if (deleteOnExit) generatedBndrunFile.deleteOnExit();
			PrintWriter writer = new PrintWriter(generatedBndrunFile, "UTF-8");
			writer.print(content.replaceAll("\\t", "    "));
			writer.close();
		} catch (IOException e) {
			throw new MavenExecutionException("Failed to create temprary bndrun file!", e);
		}
	}








}
