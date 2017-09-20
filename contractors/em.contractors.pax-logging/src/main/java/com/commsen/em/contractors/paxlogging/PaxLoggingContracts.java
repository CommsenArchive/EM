package com.commsen.em.contractors.paxlogging;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;

@Provides(value="logging")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=org.ops4j.pax.logging.pax-logging-log4j2)(version=1.10.1))'")
public class PaxLoggingContracts {}
