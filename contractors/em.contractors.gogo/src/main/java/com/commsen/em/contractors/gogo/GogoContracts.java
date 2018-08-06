package com.commsen.em.contractors.gogo;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;

@Provides( //
		value = "local.console", //
		options = { //
				"provider=gogo" //
		} //
)
@Requires(raw = "osgi.identity;filter:='(osgi.identity=org.apache.felix.gogo.jline)'")
@Requires(raw = "osgi.identity;filter:='(osgi.identity=org.apache.felix.gogo.command)'")
public class GogoContracts {}
