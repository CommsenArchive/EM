package com.commsen.em.contractors.webconsole;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.RequiresServletContainer;

@Provides("webconsole")
@Provides("WebAdminUI")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=org.apache.felix.webconsole)(version>=4.3.4))'")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.apache.felix.webconsole.plugins.ds))")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.apache.felix.webconsole.plugins.obr))")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.apache.felix.webconsole.plugins.event))")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.apache.felix.webconsole.plugins.packageadmin))")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.apache.felix.webconsole.plugins.memoryusage))")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.apache.felix.webconsole.plugins.scriptconsole))")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.apache.karaf.webconsole.http))")
@Requires(ns="osgi.identity", filter="(&(osgi.identity=org.everit.osgi.webconsole.threadviewer))")
@RequiresServletContainer(version="2.5")
public class WebConsoleContracts {}

