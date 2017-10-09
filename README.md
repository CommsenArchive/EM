
_This README describes version 0.3.0. This is a work in progress, so it may not be fully accurate! Links to released versions READMEs are provided below:_

 - [version 0.2.x](https://github.com/commsen/EM/blob/0.2.x/README.md)
 - [version 0.1.x](https://github.com/commsen/EM/blob/0.1.x/README.md)

# Eccentric Modularity

Eccentric Modularity (EM) is Maven extension making it easy to build modular, contract-based Java applications


## What it does?

EM provides simple way to move from hardcoded dependencies to artifact(jar) discovery based on generic requirements and capabilities. It allows to discover the artifacts providing the functionalities an application needs! It can also create standalone (Spring Boot like) applications!

It can:
 - augment the output jar produced by Maven to make it a module
 - resolve and provide a complete list of modules that need to be deployed together in modular runtime
 - build a standalone executable jar file with all modules


### Is it about Java9 / JPMS / JSR376 / Jigsaw modularity?

No. However it may evolve to generate JPMS modules in the future if there is any interest. The general concept "generate and add metadata to jar files so build tools can discover related jars" is a valid use case for JPMS. Yet it requires different approach which I may experiment with later on.

### Is it about OSGi?

It uses some of the OSGi specifications and tools but does not require you to learn or use OSGi at runtime!

By default, the standalone executable contains OSGi runtime in which modules are run, but developers need not to worry about it (much like they don't deal with Spring Boot's internals). It is also possible to use [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) to generate the executable JAR on your own. This makes it possible to discover and wire contractors via [Java's SPI (ServiceLoader)](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#packaging-the-dictionary-service-in-a-jar-file) if the limited features it provides is all you need.

### Is it a SpringBoot alternative?

No. It may be seen as such in the sense that it can generate standalone executable JAR file. However that is simply one possible outcome. It is not the goal of EM to compete with SpringBoot but to make is easier for developers to build highly modular applications.  

## Installation

Until all `0.3.0` artifacts are released in central, you need to build the project locally. The steps are as follows:

 - clone the project
 - run `mvn install` in the main folder to build and install locally the main artifacts
 - run `mvn install` in `contractors` folder to build and install locally the OOTB contractors
 - optionally run `mvn clean package` in `demos` folder to build the demos

## Usage

Add EM extension to your project:
```xml
<build>
  <extensions>
    <extension>
      <groupId>com.commsen.em</groupId>
      <artifactId>em-maven-extension</artifactId>
      <version>${em.version}</version>
    </extension>
  </extensions>
</build>
```

It will not do anything unless you instruct it via one of the following properties:

 - `<em:module />` - makes the jar file produced by Maven a module
 - `<em:resolve />` - saves in one place all modules that are needed to fulfill the requirements of this module
 - `<em:resolve>host:port:name:version</em:resolve>` - same as `<em:resolve />` but skips the modules already available in the runtime accessible at `host:port`
 - `<em:executable />` - builds a standalone executable jar (much like Spring Boot)

Please see [demo projects](https://github.com/commsen/EM/tree/master/demos) for examples!

_[HINT]: `em:` prefix is XML namespace which Maven ignores but proper XML editors will complain about. To get rid of the warnings you can add `xmlns:em="EM"` to `<project ...>` top level tag of the POM._

## How does it work

EM dynamically adds and configures one or more Maven plugins, based on the expected outcome specified by one of the above properties.

Any module can specify the functionalities (called contracts) it expects as well as those it provides itself. A module may declare what contracts it expects to be fulfilled in a number of ways:

  - by importing a package (this is automated process based on bytecode analysis)
  - by consuming a service (via `@Reference` annotation)
  - by explicitly stating it (via `@Requires` annotation)   

A module can also declare it fulfills contracts in a number of ways:

 - by exporting a package (via `@Export` annotation in `package-info.java`)
 - by registering a service (via `@Component` annotation)
 - by explicitly stating it (via `@Provides` annotation)   

Module may both provide and require many contracts. For example a JAX-RS module could use `@RequiresJaxrsServer` annotation to explicitly state it needs a JAX-RS server. This contract could be fulfilled by `em.contractors.jaxrs.publisher` module for example. It however uses `@RequiresServletContainer` to express the need for HTTP server. A number of modules (jetty, tomcat, ...) then may fulfill that contract, declaring own requirements at the same time.   

At build time, when EM sees `<em:resolve/>` or `<em:executable />`, it will check to see if all transitive contracts are fulfilled by the contractors specified in `<em:contractors>` property and if not it will fail the build. It will also try to suggest a contractor that potentially can fulfill the missing contract. By doing that, EM ensures there will be no missing modules at runtime, without polluting the build classpath and/or Maven dependency graph.     

## Credits and inspiration

This work was heavily influenced and borrows ideas and examples from the following projects:
 - [bnd](http://bnd.bndtools.org/) and [Bndtools](http://bndtools.org/)
 - [enRoute](http://enroute.osgi.org/)
 - [Liferay](http://liferay.com)
 - [Karaf](https://karaf.apache.org/)
 - [OSGi - JAX-RS Connector](https://github.com/hstaudacher/osgi-jax-rs-connector)

## License

The project is released under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
