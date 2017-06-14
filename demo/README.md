# Eccentric Modularity (EM) Demos

## Demo: Editor

Using requirements and capabilities to resolve a standard Java application.

 - `calc-api` - a very simple contract artifact. Contains one interface defining the "calculator contract". It uses `@Export` package-level annotation to export the package containing the contract. The `<_.eccentric.modularity.metadata />` tells EM to put this information in `MANIFEST.MF`. No dependencies (apart from annotations).
 - `calc-simple` - a naive provider of `calc-api` contract. It uses `@ProvideCapability` class-level annotation to specify it fulfills the "calculator contract". It uses [Java's SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#packaging-the-dictionary-service-in-a-jar-file) approach to register the service. The `<_.eccentric.modularity.metadata />` tells EM to put this information in `MANIFEST.MF`. It only depends on `calc-api` (apart from annotations).
 - `calc-fancy` - a better provider of `calc-api` contract based on Parsii library. It uses `@ProvideCapability` class-level annotation to specify it fulfills the "calculator contract" and supports specific math operations. It uses [Java's SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#packaging-the-dictionary-service-in-a-jar-file) approach to register the service. The `<_.eccentric.modularity.metadata />` tells EM to put this information in `MANIFEST.MF`. It only depends on `calc-api` (apart from annotations).
 - `index-calc` - a POM only index artifact. It "depends" on all providers of the "calculator contract". EM understands `index` artifacts and creates an index (available as `xml` and `xml.gz`) from the dependencies.
 - `markup` - fake markup language that can calculate math expressions inside `<math>` tags. It only depends on `calc-api` (apart from annotations). It loads a provider of the "calculator contract" via [Java's native service loader](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#packaging-the-dictionary-service-in-a-jar-file). It uses `@RequireCapability` class-level annotation to specify its requirements. The `<_.eccentric.modularity.metadata />` tells EM to put this information in `MANIFEST.MF`.
 - `editor` - A Simple text editor (Java Swing desktop application) that understands the `math` markup. It only depends on `markup` and `index-cal` (which is an index not a jar). It uses [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) to build single executable. To avoid adding dependency on specific provider it uses `<_.eccentric.modularity.resolve />` which finds a provider of the "calculator contract" based on specified requirements. It then uses a custom transformer for maven-shade-plugin to add discovered artifacts to the standalone executable.

## Demo: RESTful service

This uses calc jars above to build up a standalone RESTful math service. It uses [Declarative Services](http://enroute.osgi.org/services/org.osgi.service.component.html) annotations to wire components together.  

 - `index-rest` - a POM only index artifact. It "depends" on whole bunch of libraries and frameworks. Yet, not all those will be used at runtime. They are simply a source for EM to create another index which can later one be used to discover which artifacts are needed by which contracts.
 - `augment` - a resource only artifact that builds a jar containing metadata about other jars. It has no dependencies, only `augments.bnd` file and `<_.eccentric.modularity.augment />` property which instructs EM to mark the jar as augmenting one. The `augments.bnd` file augments two jars:  
   - `javax.ws.rs-api` - with unsatisfiable requirement so it's not picked for runtime
   - `com.eclipsesource.jaxrs.publisher` - with information it provides a whiteboard pattern based implementation of Java's JAX-RS contract
 - `rest` - A Simple RESTful service using Java JAX-RS. It only depends on `calc-api` and `javax.ws.rs-api` (apart from annotations and indexes). It uses custom `@RequireJaxrsWhiteboard` and `@RequirePowerCalculator` annotations to declare it requires a JAX-RS implementation (one that supports whiteboard pattern) and advanced calculator.
   * normal build uses default profile where `<_.eccentric.modularity.executable />` property tells EM to resolve the artifacts from provided indexes and build a standalone executable.
   * building with `-P liferay` uses `<_.eccentric.modularity.target.runtime>` property to tell EM to resolve against running Liferay instance and prepare set of artifacts to be deployed. 

See the source code for details.
