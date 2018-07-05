package com.commsen.em.maven.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.artifact.resolve.ArtifactResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.fusesource.jansi.Ansi;
import org.slf4j.LoggerFactory;

@Component(role = Dependencies.class)
public class Dependencies {
		
	private IndentingLogger logger = IndentingLogger.wrap(LoggerFactory.getLogger(Dependencies.class));

	@Requirement
	private ArtifactResolver artifactResolver;

	@Requirement
	private RepositorySystem mavenRepoSystem;
	
	public Set<Artifact> asArtifacts(ProjectBuildingRequest projectBuildingRequest, MavenProject project) throws MavenExecutionException {
		if (project.getDependencies() != null) {
			logger.info("Analysing direct dependencies tree of `{}` ...", toBold(project.getId()));
			logger.indent();
			Set<Artifact> result = asArtifacts(projectBuildingRequest, project.getId(), project.getDependencies());
			logger.unindent();
			logger.info("Done analysing direct dependencies tree of `{}`! Found {} artifacts!", toBold(project.getId()), result.size());
			return result;
		} else {
			logger.info("No direct dependencies found in `{}`", toBold(project.getId()));
			return Collections.emptySet();
		}
	}

	public Set<Artifact> managedAsArtifacts(ProjectBuildingRequest projectBuildingRequest, MavenProject project) throws MavenExecutionException {
		if (project.getDependencyManagement() != null) {
			logger.info("Analysing managed dependencies tree of `{}` ...", toBold(project.getId()));
			logger.indent();
			Set<Artifact> result = asArtifacts(projectBuildingRequest, project.getId(), project.getDependencyManagement().getDependencies());
			logger.unindent();
			logger.info("Done analysing managed dependencies tree of `{}`! Found {} artifacts!", toBold(project.getId()), result.size());
			return result;
		} else {
			logger.info("No managed dependencies found in `{}`", toBold(project.getId()));
			return Collections.emptySet();
		}
	}

	@SuppressWarnings("deprecation")
	public Set<Artifact> resolve(MavenProject project, Artifact artifact) throws MavenExecutionException {
		Set<Artifact> artifacts = new HashSet<>();
		ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest();
		artifactResolutionRequest.setResolveTransitively(true);
		artifactResolutionRequest.setRemoteRepositories(project.getRemoteArtifactRepositories());
		artifactResolutionRequest.setLocalRepository(project.getProjectBuildingRequest().getLocalRepository());
		artifactResolutionRequest.setArtifact(artifact);
		ArtifactResolutionResult artifactResolutionResult = mavenRepoSystem.resolve(artifactResolutionRequest);
		artifactResolutionResult.getArtifacts().stream()
			.peek(d -> { if (Flag.verbose()) logger.info("RESOLVED '{}' FROM '{}'", d,  artifact); })
			.filter(d -> "jar".equals(d.getType()))
			.filter(d -> "compile".equals(d.getScope()) || "provided".equals(d.getScope()) || "runtime".equals(d.getScope()))
			.forEach(d -> artifacts.add(d));
		return artifacts;
	}

	public Set<Artifact> asArtifacts(ProjectBuildingRequest projectBuildingRequest, String projectId, Collection<Dependency> initial) throws MavenExecutionException {

		ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest();
		artifactResolutionRequest.setResolveTransitively(true);
		artifactResolutionRequest.setRemoteRepositories(projectBuildingRequest.getRemoteRepositories());
		artifactResolutionRequest.setLocalRepository(projectBuildingRequest.getLocalRepository());

		Set<Artifact> artifacts = new HashSet<>();
		
		List<Dependency> jarDependencies = initial.stream()
				.filter(isDependencyRuntimeDependencyOf(projectId))
				.collect(Collectors.toList()
		);
		
		for (Dependency dependency : jarDependencies) {
			Set<Artifact> tmpArtifacts = new HashSet<>();

			// make sure to not process excluded dependencies
			Set<String> excludes = new HashSet<>();
			for (Exclusion exclusion : dependency.getExclusions()) {
				if (exclusion.getGroupId() != null) {
					excludes.add(exclusion.getGroupId() + ":" + exclusion.getArtifactId());
				} else {
					excludes.add(exclusion.getArtifactId());
				}
				if (Flag.verbose()) {
					logger.info("🚫  `{}` is excluded dependency of `{}`", toBold(exclusion.getArtifactId()), toBold(toId(dependency))); 
				} else if (logger.isDebugEnabled()) {
					logger.debug("🚫  `{}` is excluded dependency of `{}`", toBold(exclusion.getArtifactId()), toBold(toId(dependency))); 
				}
			} 
			ArtifactFilter filter = new ExclusionSetFilter(excludes);
			
			Artifact artifact = asArtifact(projectBuildingRequest, dependency);
			artifacts.add(artifact);
			artifactResolutionRequest.setArtifact(artifact);
			artifactResolutionRequest.setCollectionFilter(filter);
			ArtifactResolutionResult artifactResolutionResult = mavenRepoSystem.resolve(artifactResolutionRequest);
			artifactResolutionResult.getArtifacts().stream()
				.filter(isArtifactRuntimeDependencyOf(toId(dependency)))
				.forEach(d -> tmpArtifacts.add(d));
			artifacts.addAll(tmpArtifacts);
		}
		return artifacts;
	}



	public Artifact asArtifact (ProjectBuildingRequest projectBuildingRequest, Dependency dependency) throws MavenExecutionException {
		DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
		coordinate.setGroupId(dependency.getGroupId());
		coordinate.setArtifactId(dependency.getArtifactId());
		coordinate.setVersion(dependency.getVersion());
		coordinate.setExtension(dependency.getType());
		coordinate.setClassifier(dependency.getClassifier());

		ArtifactResult ar;
		try {
			ar = artifactResolver.resolveArtifact(projectBuildingRequest, coordinate);
		} catch (ArtifactResolverException e) {
			throw new MavenExecutionException("Failed to resolve artifact " + coordinate, e);
		}
		return ar.getArtifact();
		
	}

	public boolean isOSGiBundle (Artifact artifact) {
		try (JarFile jarFile = new JarFile(artifact.getFile()))  {
			Manifest manifest = jarFile.getManifest();
			if (manifest == null) return false;
			return manifest.getMainAttributes().getValue("Bundle-SymbolicName") != null;
		} catch (IOException e) {
			logger.warn("Failed to check if " + artifact.getFile() + " is OSGi bundle!", e);
		}
		return false;
	}
	

	public void addToDependencyManagement(MavenProject project, Dependency dependency, String from) {
		if (project.getModel().getDependencyManagement() == null) {
			project.getModel().setDependencyManagement(new DependencyManagement());
		}
		project.getModel().getDependencyManagement().addDependency(dependency);;

		if (Flag.verbose()) {
			logger.info("💉  `{}`  was added as managed dependency of `{}` because of `{}` contractor", toBold(toId(dependency)), toBold(project.getId()), toBold(from)); 
		} else if (logger.isDebugEnabled()) {
			logger.debug("💉  `{}`  was added as managed dependency of `{}` because of `{}` contractor", toBold(toId(dependency)), toBold(project.getId()), toBold(from)); 
		}
	}

	
	private String toId (Dependency dependency) {
		return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
	}
	
	private Predicate<Dependency> isDependencyRuntimeDependencyOf (String parentId) {
		return d -> {
			boolean match = 
					"jar".equals(d.getType()) && 
					(	
						"compile".equals(d.getScope()) || 
						"provided".equals(d.getScope()) || 
						"runtime".equals(d.getScope())
					);
			if (Flag.verbose()) {
				logger.info("{}  `{}` is a dependency of `{}` {}", match ? "✅" : "⛔", toBold(toId(d)), toBold(parentId), match ? "" : ", but it's not a JAR needed at runtime"); 
			} else if (logger.isDebugEnabled()) {
				logger.debug("{}  `{}` is a dependency of `{}` {}", match ? "✅" : "⛔", toBold(toId(d)), toBold(parentId), match ? "" : ", but it's not a JAR needed at runtime"); 
			}
			return match;
		};
	}

	private Predicate<Artifact> isArtifactRuntimeDependencyOf (String parentId) {
		return a -> {
			boolean match = 
					"jar".equals(a.getType()) && 
					(	
						"compile".equals(a.getScope()) || 
						"provided".equals(a.getScope()) || 
						"runtime".equals(a.getScope())
					);
			if (Flag.verbose()) {
				logger.info("{}  `{}` is a dependency of `{}` {}", match ? "✅" : "⛔", toBold(a.getId()), toBold(parentId), match ? "" : ", but it's not a JAR needed at runtime"); 
			} else if (logger.isDebugEnabled()) {
				logger.debug("{}  `{}` is a dependency of `{}` {}", match ? "✅" : "⛔", toBold(a.getId()), toBold(parentId), match ? "" : ", but it's not a JAR needed at runtime"); 
			}
			return match;
		};
	}	
	
	private String toBold(String text) {
		Ansi ansi = Ansi.ansi();
		return ansi.bold().a(text).boldOff().toString();
	}
	
}
