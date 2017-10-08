package com.commsen.em.contractors.liferay;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;

@Provides( //
		value = "servlet.container", //
		options = { //
				"whiteboard=true", //
				"version:List<Version>='2.5,3.0,3.1'" //
		})
@Requires(raw="osgi.identity;filter:='(osgi.identity=org.eclipse.equinox.http.servlet)'")
public class LiferayContracts {}
