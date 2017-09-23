package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Requires(value = "jaxrs.server", filter = RequiresJaxrsServer.filterExpression)
@Retention(RetentionPolicy.CLASS)
public @interface RequiresJaxrsServer {

	String filterExpression = "<#if provider?has_content && whiteboard>" //
			+ "(&(em.contract=jaxrs.server)(provider=${provider})(whiteboard=true))" //
			+ "<#elseif provider?has_content>" //
			+ "(&(em.contract=jaxrs.server)(provider=${provider}))" //
			+ "<#elseif whiteboard>" //
			+ "(&(em.contract=jaxrs.server)(whiteboard=true))" //
			+ "<#else>" //
			+ "(em.contract=jaxrs.server)" //
			+ "</#if>";

	String provider() default "";

	boolean whiteboard() default true;
}
