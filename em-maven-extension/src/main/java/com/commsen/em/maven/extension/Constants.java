package com.commsen.em.maven.extension;

public class Constants {

	public static final String VAL_BND_VERSION = "3.4.0";

	public static final String VAL_EXTENSION_GROUP = "com.commsen.em";
	public static final String VAL_EXTENSION_ARTIFACT = "em-maven-extension";

	public static final String VAL_INDEX_TYPE = "index";
	
	public static final String PROP_PREFIX = "_.eccentric.modularity.";

	public static final String PROP_ACTION_METADATA = PROP_PREFIX + "metadata";
	
	public static final String PROP_ACTION_AUGMENT = PROP_PREFIX + "augment";
	public static final String PROP_AUGMENT_FILE = PROP_ACTION_AUGMENT + ".file";

	/*
	 *  properties for RESOLVE action
	 */
	
	public static final String PROP_ACTION_RESOLVE = PROP_PREFIX + "resolve";
	public static final String PROP_RESOLVE_OUTPUT = PROP_ACTION_RESOLVE + ".output";

	/*
	 *  properties for EXECUTABLE action
	 */
	public static final String PROP_ACTION_EXECUTABLE = PROP_PREFIX + "executable";

	
	/*
	 *  properties for DEPLOYABLE action
	 */
	public static final String PROP_ACTION_DEPLOY = PROP_PREFIX + "deploy";
	public static final String PROP_DEPLOY_OUTPUT = PROP_ACTION_DEPLOY + ".output";

	
	/*
	 * config 
	 */
	
	public static final String CONFIG_PREFIX = PROP_PREFIX + "cfg.";
	public static final String PROP_CONFIG_INDEX = CONFIG_PREFIX + "createIndex";
	public static final String PROP_CONFIG_TMP_BUNDLES = CONFIG_PREFIX + "tempBundlesDirectory";
	public static final String PROP_CONFIG_REQUIREMENTS = CONFIG_PREFIX + "requirements";
	public static final String PROP_CONFIG_INCLUDE_PACKAGES = CONFIG_PREFIX + "includePackages";
	public static final String PROP_CONFIG_IMPORT_PACKAGES = CONFIG_PREFIX + "importPackages";
	public static final String PROP_CONFIG_IGNORE_PACKAGES = CONFIG_PREFIX + "ignorePackages";


	/*
	 * defaults 
	 */

	public static final String DEFAULT_TMP_BUNDLES = ".bundles";

	
}
