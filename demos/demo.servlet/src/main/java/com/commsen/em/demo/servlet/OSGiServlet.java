package com.commsen.em.demo.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.commsen.em.annotations.RequiresServletContainer;

@Component( //
		property = { //
				"osgi.http.whiteboard.servlet.pattern=/osgi"
		}, //
		service = Servlet.class //
)
@RequiresServletContainer(version = "3.0")
public class OSGiServlet extends HttpServlet {

	private static final long serialVersionUID = 5665272843101468811L;

	@Reference
	private HelloService helloService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write(helloService.sayHello("World") + " from OSGi R6 Whiteboard Servlet :)");
	}
}
