package com.commsen.em.web.gui;

import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class MainUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		// The root of the component hierarchy
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull(); // Use entire window
		setContent(content); // Attach to the UI

		
		TextField search = new TextField("Search");
		Label l = new Label("Hello");

		search.addValueChangeListener(event -> {
			l.setValue(event.getValue());
		});
		
		search.setValueChangeMode(ValueChangeMode.TIMEOUT);
		search.setValueChangeTimeout(2*1000);
		
		// Add some component
		content.addComponent(search);
		content.addComponent(l);
		
		

	}
}