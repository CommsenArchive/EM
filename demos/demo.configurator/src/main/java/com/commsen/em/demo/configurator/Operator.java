package com.commsen.em.demo.configurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(immediate = true)
public class Operator {

	@Reference(
				policy = ReferencePolicy.DYNAMIC, 
				policyOption = ReferencePolicyOption.GREEDY, 
				updated="referenceUpdated"
				)
	private volatile ReaderService reader;
	
	@Reference(
				policy = ReferencePolicy.DYNAMIC, 
				policyOption = ReferencePolicyOption.GREEDY, 
				updated="referenceUpdated"
				)
	private volatile WriterService writer;

	

	
	@Activate
	public void activate() {
		System.out.println("┌---- Operator ACTIVATED -----");
		System.out.println("|  READER: " + (reader == null ? " none" : reader.getSource()));
		System.out.println("|  WRITER: " + (writer == null ? " none" : writer.getDestination()));
		System.out.println("└-----------------------------");
	}

	@Modified
	public void modified() {
		System.out.println("┌---- Operator MODIFIED -----");
		System.out.println("|  READER: " + (reader == null ? " none" : reader.getSource()));
		System.out.println("|  WRITER: " + (writer == null ? " none" : writer.getDestination()));
		System.out.println("└----------------------------");
	}

	@Deactivate
	public void deactivated() {
		System.out.println("┌---- Operator DEACTIVATED -----");
		System.out.println("|  READER: " + (reader == null ? " none" : reader.getSource()));
		System.out.println("|  WRITER: " + (writer == null ? " none" : writer.getDestination()));
		System.out.println("└-------------------------------");
	}

	public void referenceUpdated() {
		System.out.println("┌---- Operator references updated -----");
		System.out.println("|  READER: " + (reader == null ? " none" : reader.getSource()));
		System.out.println("|  WRITER: " + (writer == null ? " none" : writer.getDestination()));
		System.out.println("└--------------------------------------");
	}

}
