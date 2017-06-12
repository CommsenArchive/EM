package com.commsen.em.demo.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import aQute.bnd.annotation.headers.RequireCapability;

@RequireCapability (
		ns="eccentric.modularity.demo",
		filter="(&(eccentric.modularity.demo=calculator)(power=true))",
		effective="assemble"
		)
@Retention(RetentionPolicy.CLASS)
public @interface RequirePowerCalculator {

}
