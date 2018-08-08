package com.commsen.em.demo.configurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.commsen.em.annotations.RequiresConfigurator;

@Component (service=WriterService.class)
@Designate (ocd=WriterConfiguration.class)
@RequiresConfigurator
public class WriterService {

	WriterConfiguration configuration;
	
	@Activate
	public void activate (WriterConfiguration configuration) {
		this.configuration = configuration;
		System.out.println("┌---- WriterService ACTIVATED -----");
		System.out.println("|  " + getDestination());
		System.out.println("└-----------------------------");
	}

	@Modified
	public void modified (WriterConfiguration configuration) {
		this.configuration = configuration;
		System.out.println("┌---- WriterService MODIFIED -----");
		System.out.println("|  " + getDestination());
		System.out.println("└-----------------------------");
	}

	@Deactivate
	public void deactivate (WriterConfiguration configuration) {
		this.configuration = configuration;
		System.out.println("┌---- WriterService DEACTIVATED -----");
		System.out.println("|  " + getDestination());
		System.out.println("└-----------------------------");
	}


	public String getDestination() {
		return configuration.host() + ":" + configuration.port() + ":" + (configuration.retry() ? "retry" : "single"); 
	}
}
