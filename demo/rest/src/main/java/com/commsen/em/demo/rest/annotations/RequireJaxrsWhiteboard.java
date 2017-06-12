package com.commsen.em.demo.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import aQute.bnd.annotation.headers.RequireCapability;

@RequireCapability (
		ns="osgi.contract",
		filter="(&(osgi.contract=JavaJAXRS)(whiteboard=true))",
		effective="assemble"
		)
@Retention(RetentionPolicy.CLASS)
public @interface RequireJaxrsWhiteboard {

}
