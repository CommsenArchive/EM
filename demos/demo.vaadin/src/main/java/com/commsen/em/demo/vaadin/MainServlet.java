package com.commsen.em.demo.vaadin;

import javax.servlet.annotation.WebServlet;

import org.osgi.service.component.annotations.Component;

import com.commsen.em.annotations.Requires;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

@Component(//
		service = VaadinServlet.class
)
@WebServlet( //
		urlPatterns = "/main/*", //
		name = "MainServlet", //
		asyncSupported = true //
)
@VaadinServletConfiguration(//
		ui = MainUI.class, //
		productionMode = false //
)
@Requires("vaadin")
public class MainServlet extends VaadinServlet {

	private static final long serialVersionUID = 1L;
}
