package com.commsen.em.contract.vaadin;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;

@Provides(value="vaadin", version="8.1.3")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=com.vaadin.osgi.integration)(version=8.1.3))'")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=com.vaadin.themes)(version=8.1.3))'")
@Requires(raw="osgi.identity;filter:='(&(osgi.identity=com.vaadin.client-compiled)(version=8.1.3))'")
@Requires(filter="(&(em.contract=servlet.container)(whiteboard=true)(version=6))")
public class VaadinContracts {}
