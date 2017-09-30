package com.commsen.em.demo.command.gogo;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.osgi.service.component.annotations.Component;

import com.commsen.em.annotations.RequiresLocalConsole;

@Component( //
		property = { //
				"osgi.command.function=hello", //
				"osgi.command.function=greet", //
				"osgi.command.scope=em" //
		}, //
		service = Object.class //
)
@RequiresLocalConsole
public class HelloCommand {
	
	@Descriptor("Says hello")
	public void hello () {
		System.out.println("Hello there!");
	}
	
	@Descriptor("Greets someone")
	public void greet (@Parameter(names="-n", absentValue="stranger") String name) {
		System.out.println("Hello " + name + "!");
	}

}
