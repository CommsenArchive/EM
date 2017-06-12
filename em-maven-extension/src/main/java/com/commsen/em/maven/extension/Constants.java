package com.commsen.em.maven.extension;

public class Constants {

	public static final String VAL_EXTENSION_GROUP = "com.commsen.em";
	public static final String VAL_EXTENSION_ARTIFACT = "em-maven-extension";

	public static final String VAL_INDEX_TYPE = "index";
	
	public static final String PROP_PREFIX = "_.eccentric.modularity.";

	public static final String PROP_ACTION_METADATA = PROP_PREFIX + "metadata";
	public static final String PROP_ACTION_AUGMENT = PROP_PREFIX + "augment";
	public static final String PROP_ACTION_RESOLVE = PROP_PREFIX + "resolve";
	public static final String PROP_ACTION_EXECUTABLE_OSGI = PROP_PREFIX + "executable";

	public static final String PROP_RESOLVE_OUTPUT = PROP_ACTION_RESOLVE + ".output";
	
}
