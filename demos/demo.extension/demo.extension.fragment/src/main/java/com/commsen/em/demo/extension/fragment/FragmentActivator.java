package com.commsen.em.demo.extension.fragment;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.commsen.em.annotations.Activator;
import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.RuntimeExtension;


@RuntimeExtension
@Activator(extension=true)
@Provides("DemoFrameworkExtension")
public class FragmentActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.setProperty("em.demo.custom.system.property", "success");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
