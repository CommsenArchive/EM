
# Eccentric Modularity

Eccentric Modularity is Maven extension making it easy to build modular, contract based Java applications

## What it does?

Eccentric Modularity provides simple way to move from hardcoded dependencies to artifact(jar) discovery based on generic requirements and capabilities. It allows to discover the artifacts providing the functionalities an application needs! It can also create standalone (Spring Boot like) applications!

### Is it about Java9 / JPMS / JSR376 / Jigsaw modularity?

No! However it may evolve to generate JPMS wirings in the future if there is any interest. The general concept "generate and add metadata to jar files so build tools can discover related jars" is a valid use case for JPMS. Yet it requires different approach which I may experiment with once Java 9 is released.

### Is it about OSGi?

It uses some of the OSGi specifications and tools but does not require you to learn or use OSGi at runtime! In the demos, you have an example of an app (editor) which can discover a contract provider(s) and wire it via [Java's SPI (ServiceLoader)](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#packaging-the-dictionary-service-in-a-jar-file). The other example (rest) indeed uses OSGi runtime but you should not need to deal with it (much like you don't deal with Spring Boot's internals).

## Installation

Simply clone the project and run `mvn install`. It is not available in maven central as it is because ... well it's a PoC :) Once it evolves (if ever) to a real project I'll add it to Maven central.

## Usage

You need to add Eccentric Modularity extension to your project:
```xml
<build>
  <extensions>
    <extension>
      <groupId>com.commsen.em</groupId>
      <artifactId>em-maven-extension</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </extension>
  </extensions>
</build>
```

It will not do anything unless you instruct it via one of the following properties

 - `<_.eccentric.modularity.metadata />` - generates and adds metadata to MANIFEST.MF file of the jar based on annotations
 - `<_.eccentric.modularity.augment />` - generates a jar file that only contains metadata about other jar functionalities
 - `<_.eccentric.modularity.resolve />` - saves in one place all jars that are needed to fulfill the requirements of your applications
 - `<_.eccentric.modularity.executable />` - builds a single executable jar (much like Spring Boot)
 - `<_.eccentric.modularity.deploy />` - saves in one place all jars that needed to be deployed to given target runtime 

Please see [demo projects](https://github.com/azzazzel/EM/tree/master/demo) for examples!

## How does it work

Eccentric Modularity dynamically adds and configures one or more of [bnd maven plugins](https://github.com/bndtools/bnd/tree/master/maven) to the project, based on the expected outcome specified by one of the above properties. The exact same result can be achieved by manually adding and configuring the plugins if more fine tuned solution is needed.    

## Credits and inspiration

This work was heavily influenced and borrows ideas and examples from the following projects:
 - [bnd](http://bnd.bndtools.org/) and [Bndtools](http://bndtools.org/)
 - [enRoute](http://enroute.osgi.org/)
 - [Liferay](http://liferay.com)
 - [Karaf](https://karaf.apache.org/)
 - [OSGi - JAX-RS Connector](https://github.com/hstaudacher/osgi-jax-rs-connector)

## License

The project is released under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

