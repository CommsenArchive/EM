package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Requires(value="scheduler", filter=RequiresScheduler.filterExpression)
@Retention(RetentionPolicy.CLASS)
public @interface RequiresScheduler {

	String filterExpression = "<#if provider?has_content>(&(em.contract=scheduler)(provider=${provider}))<#else>(em.contract=scheduler)</#if>";
	
	String provider() default "";
}
