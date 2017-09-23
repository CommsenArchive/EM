package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Requires(value="local.console", filter=RequiresLocalConsole.filterExpression)
@Retention(RetentionPolicy.CLASS)
public @interface RequiresLocalConsole {
	
	String filterExpression = "<#if provider?has_content>(&(em.contract=local.console)(provider=${provider}))<#else>(em.contract=local.console)</#if>";
	
	String provider() default "";
	
}

