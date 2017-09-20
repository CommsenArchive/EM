package com.commsen.em.contractors.paxweb.undertow;
import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;

@Provides( //
		value = "servlet.container", //
		options = { //
				"whiteboard=true", //
				"version:List<Version>='2.5,3.0,3.1'" //
		})
@Requires("logging")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=org.ops4j.pax.web.pax-web-extender-whiteboard)(version=6.0.7))'")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=org.ops4j.pax.web.pax-web-undertow)(version=6.0.7))'")
@Requires(raw="osgi.service;filter:='(objectClass=org.osgi.service.http.HttpService)';effective:=active")
public class PaxWebUndertowContracts {}
