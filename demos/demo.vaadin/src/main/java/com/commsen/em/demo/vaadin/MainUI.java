package com.commsen.em.demo.vaadin;

import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class MainUI extends UI {

	private static final long serialVersionUID = 1L;

	@Override
	protected void init(VaadinRequest request) {
		// The root of the component hierarchy
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull(); // Use entire window
		setContent(content); // Attach to the UI

		// Add some component
		content.addComponent(new Label("<b>Hello!</b> - How are you?", ContentMode.HTML));

	}
}