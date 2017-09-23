package com.commsen.em.demo.configurator;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition (name="Writer Configuration")
public @interface WriterConfiguration {

	@AttributeDefinition (name = "Server's host name")
	String host() default "Demo with metatypes";

	@AttributeDefinition (name = "Server's port", min="1000", max="65535")
	int port() default 8888;

	@AttributeDefinition (name = "Retry on error", required=false)
	boolean retry() default false;

}
