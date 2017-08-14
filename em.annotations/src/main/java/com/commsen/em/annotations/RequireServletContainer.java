package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import aQute.bnd.annotation.headers.RequireCapability;

@RequireCapability (
		ns="osgi.implementation",
		filter="(osgi.implementation=osgi.http)",
		effective="assemble"
		)
@Retention(RetentionPolicy.CLASS)
public @interface RequireServletContainer {

}
