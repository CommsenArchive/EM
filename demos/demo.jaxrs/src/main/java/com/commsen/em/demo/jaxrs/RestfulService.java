package com.commsen.em.demo.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;

import com.commsen.em.annotations.RequiresJaxrsServer;

@Path("/rest")
@Component(immediate = true, service = Object.class)
@RequiresJaxrsServer
public class RestfulService {

	@GET
	@Path("/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public String check(@PathParam ("name") String name) throws Exception {
		return "{\"message\":\"Hello " + name + "\"}";
	}
}
