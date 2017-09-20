package com.commsen.em.demo.configurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import com.commsen.em.annotations.RequiresConfigurator;

@Component (service=WriterService.class)
@Designate (ocd=WriterConfiguration.class)
@RequiresConfigurator
public class WriterService {

	WriterConfiguration configuration;
	
	@Activate
	public void start (WriterConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getDestination() {
		return configuration.host() + ":" + configuration.port() + ":" + (configuration.retry() ? "retry" : "single"); 
	}
}
