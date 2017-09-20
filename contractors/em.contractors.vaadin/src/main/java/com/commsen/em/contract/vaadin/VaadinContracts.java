package com.commsen.em.contract.vaadin;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.RequiresServletContainer;

@Provides(value="vaadin", version="8.1.3")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=com.vaadin.osgi.integration)(version=8.1.3))'")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=com.vaadin.themes)(version=8.1.3))'")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=com.vaadin.client-compiled)(version=8.1.3))'")
@RequiresServletContainer(version="3.0")
public class VaadinContracts {}
