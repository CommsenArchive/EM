package com.commsen.em.demo.extension.runtime;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.commsen.em.annotations.Activator;
import com.commsen.em.annotations.Requires;

@Activator
@Requires("DemoFrameworkExtension")
public class ModuleActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Testing system property added by framework extension: " + System.getProperty("em.demo.custom.system.property"));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
