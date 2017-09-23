package com.commsen.em.contractors.jaxrs.publisher;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.RequiresServletContainer;

@Provides(value = "jaxrs.server", options= {"whiteboard=true"})
@RequiresServletContainer
@Requires(raw = "osgi.identity;filter:='(osgi.identity=com.eclipsesource.jaxrs.publisher)'")
public class JaxrsPublisherContracts {}
