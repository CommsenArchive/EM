package com.commsen.em.demo.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.commsen.em.demo.calc.api.Calculator;
import com.commsen.em.demo.rest.annotations.RequirePowerCalculator;
import com.commsen.em.demo.rest.annotations.RequireJaxrsWhiteboard;

@Component (immediate = true, service = Object.class)
@RequireJaxrsWhiteboard
@RequirePowerCalculator
@Path("/calc")
public class CalcRestService {

	@Reference
	Calculator calculator;
	
	@GET
	@Path("/{expression}")
	@Produces(MediaType.TEXT_PLAIN)
	public Number calc(@PathParam ("expression") String expression) throws Exception {
		
		return calculator.calculate(expression);
		
	}
}
