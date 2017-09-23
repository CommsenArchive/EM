package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Requires(value="logging", filter=RequiresLogging.filterExpression)
@Retention(RetentionPolicy.CLASS)
public @interface RequiresLogging {

	String filterExpression = "<#if provider?has_content>(&(em.contract=logging)(provider=${provider}))<#else>(em.contract=logging)</#if>";
	
	String provider() default "";
}
