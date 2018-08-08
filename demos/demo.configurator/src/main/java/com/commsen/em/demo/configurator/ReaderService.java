package com.commsen.em.demo.configurator;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.RequiresConfigurator;

@Component (configurationPid="em.demo.ReaderService", service=ReaderService.class)
@RequiresConfigurator
@Requires("logging")
public class ReaderService {

	Map<String, Object> configuration;
	
	@Activate
	public void activate (Map<String, Object> configuration) {
		this.configuration = configuration;
		System.out.println("┌---- ReaderService ACTIVATED -----");
		System.out.println("|  " + getSource());
		System.out.println("└-----------------------------");
	}

	@Modified
	public void modified (Map<String, Object> configuration) {
		this.configuration = configuration;
		System.out.println("┌---- ReaderService MODIFIED -----");
		System.out.println("|  " + getSource());
		System.out.println("└-----------------------------");
	}

	@Deactivate
	public void deactivate (Map<String, Object> configuration) {
		this.configuration = configuration;
		System.out.println("┌---- ReaderService DEACTIVATED -----");
		System.out.println("|  " + getSource());
		System.out.println("└-----------------------------");
	}
	public String getSource() {
		return configuration.get("host") + ":" + configuration.get("port") + ":" + configuration.get("path"); 
	}
}
