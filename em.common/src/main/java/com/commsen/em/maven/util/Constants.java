package com.commsen.em.maven.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.maven.project.MavenProject;

public class Constants {

	private static Properties VERSIONS = new Properties();

	static {
		InputStream resourceAsStream = Constants.class.getClassLoader().getResourceAsStream("META-INF/versions.properties");
		try {
			VERSIONS.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final String VAL_EM_HOME = System.getProperty("user.home") + "/.em";
			
	public static final String VAL_BND_VERSION = getVersion("bnd");

	public static final String VAL_EXTENSION_GROUP = "com.commsen.em";
	public static final String VAL_EXTENSION_ARTIFACT = "em-maven-extension";
	public static final String VAL_EXTENSION_VERSION = getVersion("em");

	public static final String VAL_INDEX_TYPE = "index";

	public static final String PROP_PREFIX = "em:";

	/*
	 * properties for MODULE action
	 */
	public static final String PROP_ACTION_MODULE = PROP_PREFIX + "module";
	public static final String PROP_MODULE_INCLUDE_PACKAGES = PROP_ACTION_MODULE + ".includePackages";
	public static final String PROP_MODULE_IMPORT_PACKAGES = PROP_ACTION_MODULE + ".importPackages";
	public static final String PROP_MODULE_IGNORE_PACKAGES = PROP_ACTION_MODULE + ".ignorePackages";

	/*
	 * properties for AUGMENT action
	 */

	public static final String PROP_ACTION_AUGMENT = PROP_PREFIX + "augment";
	public static final String PROP_AUGMENT_FILE = PROP_ACTION_AUGMENT + ".file";

	/*
	 * properties for RESOLVE action
	 */

	public static final String PROP_ACTION_RESOLVE = PROP_PREFIX + "resolve";
	public static final String PROP_RESOLVE_OUTPUT = PROP_ACTION_RESOLVE + ".output";

	/*
	 * properties for EXECUTABLE action
	 */
	public static final String PROP_ACTION_EXECUTABLE = PROP_PREFIX + "executable";
	public static final String PROP_EXECUTABLE_RUN_PROPERTIES = PROP_ACTION_EXECUTABLE + ".properties";

	/*
	 * properties for action instructions
	 */
	public static final String PROP_CONTRACTORS = PROP_PREFIX + "contractors";
	public static final String PROP_CONTRACTS = PROP_PREFIX + "contracts";

	/*
	 * EM configuration properties
	 */

	// prefix for all configuration properties
	public static final String CONFIG_PREFIX = PROP_PREFIX + "config.";
	// should an index be generated
	public static final String PROP_CONFIG_INDEX = CONFIG_PREFIX + "createIndex";

	/*
	 * properties for internal cross-plugin communication
	 */

	// prefix for all properties
	public static final String INTERNAL_PREFIX = PROP_PREFIX + "internal.";
	public static final String INTERNAL_DISTRO_FILE = INTERNAL_PREFIX + "distro";

	/*
	 * old properties not used anymore
	 */
	public static final String PROP_PREFIX_OLD = "_.eccentric.modularity.";
	public static final String PROP_ACTION_MODULE_OLD = PROP_PREFIX + "metadata";

	public static Path getHome (MavenProject project) throws IOException {
		Path path = Paths.get(VAL_EM_HOME, "projects", project.getGroupId(), project.getArtifactId(), project.getVersion());
		createDirectoryIfDoesNotExists(path);
		return path;
	}

	public static Path getModulesFolder (MavenProject project) throws IOException {
		Path path = getHome(project).resolve("modules");
		createDirectoryIfDoesNotExists(path);
		return path;
	}

	public static Path getGeneratedModulesFolder (MavenProject project) throws IOException {
		Path path = getHome(project).resolve("generatedModules");
		createDirectoryIfDoesNotExists(path);
		return path;
	}

	public static Path getDistroFolder (MavenProject project) throws IOException {
		Path path = getHome(project).resolve("distros");
		createDirectoryIfDoesNotExists(path);
		return path;
	}

	/**
	 * @param path
	 * @throws IOException
	 */
	private static void createDirectoryIfDoesNotExists(Path path) throws IOException {
		if (!path.toFile().exists()) {
			Files.createDirectories(path);
		}
		if (!path.toFile().isDirectory()) {
			throw new IOException("Path " + path + " exists but is not a directory!");
		}
	}
	
	private static String getVersion(String key) {
		return VERSIONS.getProperty(key);
	}

}
