package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import aQute.bnd.annotation.headers.RequireCapability;

@RequireCapability (
		ns="em.contract",
		filter="$[buildFilter;em.contract;configurer;${conditions};${customFilter}]",
		effective="assemble"
		)
@Retention(RetentionPolicy.CLASS)
public @interface RequireConfigurer {

	String conditions() default "∅";

	String customFilter() default "∅";
}

