# Eccentric Modularity Demos

This folder contains very simple projects demonstrating how to use EM to build modules that depend on functionality provided by other modules.

## Brief project descriptions

  - [`demo.command`](https://github.com/commsen/EM/tree/master/demos/demo.command) - demonstrates how to provide own commands to the local console. This is a module that requires a contractor fulfilling the `local.console` contract.
  - [`demo.configurator`](https://github.com/commsen/EM/tree/master/demos/demo.configurator) - demonstrates how to configure modules. This is a module that requires a contractor fulfilling the OOTB `@RequiresConfigurator` contract.
  - [`demo.extension`](https://github.com/commsen/EM/tree/master/demos/demo.extension) - demonstrates how to extend the modular runtime. This is a module that requires a contractor fulfilling the custom `DemoFrameworkExtension` contract.
  - [`demo.jaxrs`](https://github.com/commsen/EM/tree/master/demos/demo.jaxrs) - demonstrates how to build standard JAX-RS services as modules. This is a module that requires a contractor fulfilling the OOTB `@RequiresJaxrsServer` contract.
  - [`demo.scheduler`](https://github.com/commsen/EM/tree/master/demos/demo.scheduler) - demonstrates how to build a module providing a scheduling services. This is a module that requires a contractor fulfilling the OOTB `@RequiresScheduler` contract.
  - [`demo.servlet`](https://github.com/commsen/EM/tree/master/demos/demo.servlet) - demonstrates how to build servlets as modules. This is a module that requires a contractor fulfilling the OOTB `@RequiresServletContainer` contract.
  - [`demo.vaadin`](https://github.com/commsen/EM/tree/master/demos/demo.vaadin) - demonstrates how to build [Vaadin](https://vaadin.com/) applications as modules. This is a module that requires a contractor fulfilling the `vaadin` contract.
