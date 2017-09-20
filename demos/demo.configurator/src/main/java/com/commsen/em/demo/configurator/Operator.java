package com.commsen.em.demo.configurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(immediate=true)
public class Operator {

	@Reference(policyOption=ReferencePolicyOption.GREEDY) ReaderService reader;
	@Reference(policy=ReferencePolicy.STATIC, policyOption=ReferencePolicyOption.GREEDY) WriterService writer;
	
	@Activate
	public void run () {
		System.out.println("READER: " + reader.getSource());
		System.out.println("WRITER: " + writer.getDestination());
		System.out.println("-----");
	}
}
