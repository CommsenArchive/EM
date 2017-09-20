package com.commsen.em.demo.configurator;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.RequiresConfigurator;

@Component (configurationPid="em.demo.ReaderService", service=ReaderService.class)
@RequiresConfigurator
@Requires("logging")
public class ReaderService {

	Map<String, Object> configuration;
	
	@Activate
	public void start (Map<String, Object> configuration) {
		this.configuration = configuration;
	}
	
	public String getSource() {
		return configuration.get("host") + ":" + configuration.get("port") + ":" + configuration.get("path"); 
	}
}
