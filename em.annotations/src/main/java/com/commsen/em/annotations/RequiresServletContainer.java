package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Requires(value = "servlet.container", filter="(&(em.contract=servlet.container)(version=${version})(whiteboard=${whiteboard?c}))")
@Retention(RetentionPolicy.CLASS)
public @interface RequiresServletContainer {
	
	String version() default "2.5";
	
	boolean whiteboard() default true; 
}
