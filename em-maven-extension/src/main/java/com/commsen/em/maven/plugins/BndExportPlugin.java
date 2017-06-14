package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.PROP_ACTION_RESOLVE;
import static com.commsen.em.maven.extension.Constants.PROP_RESOLVE_OUTPUT;
import static com.commsen.em.maven.extension.Constants.PROP_TARGET_RUNTIME_OUTPUT;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.artifact.resolve.ArtifactResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = BndExportPlugin.class)
public class BndExportPlugin extends DynamicMavenPlugin {

	private Logger logger = LoggerFactory.getLogger(BndExportPlugin.class);

	private WeakHashMap<String, File> indexCache = new WeakHashMap<>();

	@Requirement
	private ArtifactResolver artifactResolver;

	private List<File> filesToCleanup = new LinkedList<>();

	public void addToPomForExport(MavenProject project) throws MavenExecutionException {

		String bndrunName = getBndrunName(project);
		addToPom(project, true, bndrunName);
		project.getProperties().setProperty(PROP_RESOLVE_OUTPUT, "${project.build.directory}/export/" + bndrunName);
	}

	public void addToPomForExecutable(MavenProject project) throws MavenExecutionException {

		String bndrunName = getBndrunName(project);
		addToPom(project, false, bndrunName);
	}

	public void addToPom(MavenProject project, boolean bundlesOnly, String bndrun) throws MavenExecutionException {
		String configuration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
				+ "<configuration>" //
				+ "		<failOnChanges>false</failOnChanges>" //
				+ "		<resolve>true</resolve>" //
				+ "		<bundlesOnly>" + bundlesOnly + "</bundlesOnly>" //
				+ "		<bndruns>" //
				+ "			<bndrun>" + bndrun + "</bndrun>" //
				+ "		</bndruns>" //
				+ "		<useMavenDependencies>false</useMavenDependencies>" //
				+ "</configuration>"; //

		Plugin plugin = createPlugin("biz.aQute.bnd", "bnd-export-maven-plugin", "3.4.0-SNAPSHOT", configuration,
				"export", "export", "package");
		project.getBuild().getPlugins().add(0, plugin);

	}

	public void cleanup() {
		for (File file : filesToCleanup) {
			file.delete();
		}
	}

	private String getBndrunName(MavenProject project) throws MavenExecutionException {
		String bndrunName = project.getProperties().getProperty(PROP_ACTION_RESOLVE + ".bndrun", "");
		String distro = project.getProperties().getProperty(PROP_TARGET_RUNTIME_OUTPUT, "");
		
		List<URI> indexesFromDependencies = getIndexesFromDependencies(project);
		List<URI> indexesFromProperties = getIndexesFromProperties(project);

		List<URI> indexes = new LinkedList<>();
		indexes.addAll(indexesFromDependencies);
		indexes.addAll(indexesFromProperties);

		if (bndrunName.trim().isEmpty()) {
			bndrunName = project.getName();

			File generatedBndrunFile = new File(project.getBasedir(), bndrunName);
			if (generatedBndrunFile.exists()) {
				throw new MavenExecutionException("File " + generatedBndrunFile + " already exists!",
						project.getFile());
			}
			
			if (distro.trim().isEmpty()) {
				generateBndrun(project, generatedBndrunFile, indexes);
			} else {
				generateBndrun4Distro(project, distro, generatedBndrunFile, indexes);
			}
			filesToCleanup.add(generatedBndrunFile);

		} else {
			if (!indexesFromDependencies.isEmpty()) {
				logger.warn(
						"Custom bndrun file is provided! The following indexes found in repositories will be ignored: "
								+ indexesFromDependencies);
			}
			if (!indexesFromProperties.isEmpty()) {
				logger.warn(
						"Custom bndrun file is provided! The following indexes found in properties will be ignored: "
								+ indexesFromProperties);
			}
			if (bndrunName.endsWith(".bndrun")) {
				bndrunName = bndrunName.substring(0, bndrunName.length() - ".bndrun".length());
			}
		}

		return bndrunName;
	}

	private void generateBndrun(MavenProject project, File bndFile, List<URI> indexes) throws MavenExecutionException {

		String bndrunContent = "-standalone:\n" //
				+ "-plugin.project.external.repos: \\\n" //
				+ "		aQute.bnd.deployer.repository.FixedIndexedRepo; \\\n" //
				+ "			name		=   project.external.repos; \\\n" //
				+ "			locations 	= 	'" + indexes.stream().map(URI::toString).collect(Collectors.joining(","))
				+ "'\n" //
				+ "-plugin.project.deps.repo: \\\n" //
				+ "		aQute.bnd.deployer.repository.LocalIndexedRepo; \\\n" //
				+ "			name     =    project.deps.repo; \\\n" //
				+ "			pretty   =    true ; \\\n" //
				+ "			local    =    ${.}/target/index\n" //
				+ "-runee: JavaSE-1.8\n" //
				+ "-runfw: org.eclipse.osgi;version='[3.10,4)'\n" //
				+ "-resolve.effective: resolve, assemble\n" //
				+ "-runrequires: \\\n" //
				+ "		osgi.identity;filter:='(osgi.identity=" + project.getArtifactId() + ")'";
		writeBndrun(bndFile, bndrunContent);
	}
	
	private void generateBndrun4Distro(MavenProject project, String distro, File bndFile, List<URI> indexes) throws MavenExecutionException {

		String bndrunContent = "-standalone:\n" //
				+ "-distro: " + distro + ";version=file \n" //
				+ "-plugin.project.external.repos: \\\n" //
				+ "		aQute.bnd.deployer.repository.FixedIndexedRepo; \\\n" //
				+ "			name		=   project.external.repos; \\\n" //
				+ "			locations 	= 	'" + indexes.stream().map(URI::toString).collect(Collectors.joining(","))
				+ "'\n" //
				+ "-plugin.project.deps.repo: \\\n" //
				+ "		aQute.bnd.deployer.repository.LocalIndexedRepo; \\\n" //
				+ "			name     =    project.deps.repo; \\\n" //
				+ "			pretty   =    true ; \\\n" //
				+ "			local    =    ${.}/target/index\n" //
				+ "-resolve.effective: resolve, assemble\n" //
				+ "-runrequires: \\\n" //
				+ "		osgi.identity;filter:='(osgi.identity=" + project.getArtifactId() + ")'";
		writeBndrun(bndFile, bndrunContent);
	}

	private void writeBndrun(File generatedBndrunFile, String content) throws MavenExecutionException {
		try {
			generatedBndrunFile.createNewFile();
			PrintWriter writer = new PrintWriter(generatedBndrunFile, "UTF-8");
			writer.print(content.replaceAll("\\t", "    "));
			writer.close();
		} catch (IOException e) {
			throw new MavenExecutionException("Failed to create temprary bndrun file!", e);
		}
	}

	private List<URI> getIndexesFromProperties(MavenProject project) throws MavenExecutionException {
		List<URI> indexes = new LinkedList<>();
		String indexesURIs = project.getProperties().getProperty(PROP_ACTION_RESOLVE + ".indexes", "");
		if (!indexesURIs.trim().isEmpty()) {
			indexes = Arrays.stream(indexesURIs.split(",")).map(s -> URI.create(s)).collect(Collectors.toList());
		}
		return indexes;
	}

	private List<URI> getIndexesFromDependencies(MavenProject project) throws MavenExecutionException {
		List<URI> indexes = new LinkedList<>();
		for (Iterator<Dependency> iterator = project.getDependencies().iterator(); iterator.hasNext();) {
			Dependency dependency = iterator.next();
			if ("index".equals(dependency.getType())) {
				logger.info("  - Found index dependency " + dependency.getGroupId() + ":" + dependency.getArtifactId()
						+ ":" + dependency.getVersion());
				DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
				coordinate.setGroupId(dependency.getGroupId());
				coordinate.setArtifactId(dependency.getArtifactId());
				coordinate.setVersion(dependency.getVersion());
				coordinate.setExtension("xml.gz");

				File file = indexCache.get(coordinate.toString());

				if (file == null) {
					ArtifactResult ar;
					try {
						ar = artifactResolver.resolveArtifact(project.getProjectBuildingRequest(), coordinate);
					} catch (ArtifactResolverException e) {
						throw new MavenExecutionException("Failed to resolve artifact " + coordinate, e);
					}
					file = ar.getArtifact().getFile();
					indexCache.put(coordinate.toString(), file);
				}

				logger.info("  - Using index: " + file);
				indexes.add(file.toURI());
			}

		}

		return indexes;
	}

}
