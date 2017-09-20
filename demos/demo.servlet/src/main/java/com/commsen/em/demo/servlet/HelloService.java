package com.commsen.em.demo.servlet;

import org.osgi.service.component.annotations.Component;

@Component (service=HelloService.class)
public class HelloService {

	public String sayHello(String name) {
		return "Hello " + name + " !!!";
	}
}
