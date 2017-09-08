package com.commsen.em.maven.util;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.artifact.resolve.ArtifactResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = Dependencies.class)
public class Dependencies {
		
	private Logger logger = LoggerFactory.getLogger(Dependencies.class);

	@Requirement
	private ArtifactResolver artifactResolver;

	@Requirement
	private RepositorySystem mavenRepoSystem;
	
	public Set<Artifact> asArtifacts(MavenProject project) throws MavenExecutionException {
		return asArtifacts(project, project.getDependencies());
	}

	public Set<Artifact> mangedAsArtifacts(MavenProject project) throws MavenExecutionException {
		if (project.getDependencyManagement() != null) {
			return asArtifacts(project, project.getDependencyManagement().getDependencies());
		} else {
			return Collections.emptySet();
		}
	}

	@SuppressWarnings("deprecation")
	public Set<Artifact> asArtifacts(MavenProject project, List<Dependency> initial) throws MavenExecutionException {

		ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest();
		artifactResolutionRequest.setResolveTransitively(true);
		artifactResolutionRequest.setRemoteRepositories(project.getRemoteArtifactRepositories());
		artifactResolutionRequest.setLocalRepository(project.getProjectBuildingRequest().getLocalRepository());

		Set<Artifact> artifacts = new HashSet<>();
		
		List<Dependency> jarDependencies = initial.stream()
				.filter(d -> "jar".equals(d.getType()))
				.filter(d -> "compile".equals(d.getScope()) || "provided".equals(d.getScope()) || "runtime".equals(d.getScope()))
				.collect(Collectors.toList()
		);
		
		for (Dependency dependency : jarDependencies) {
			Artifact artifact = asArtifact(project, dependency);
			artifacts.add(artifact);
			artifactResolutionRequest.setArtifact(artifact);
			ArtifactResolutionResult artifactResolutionResult = mavenRepoSystem.resolve(artifactResolutionRequest);
			artifactResolutionResult.getArtifacts().stream()
				.filter(d -> "jar".equals(d.getType()))
				.filter(d -> "compile".equals(d.getScope()) || "provided".equals(d.getScope()) || "runtime".equals(d.getScope()))
				.forEach(d -> artifacts.add(d));
		}
		return artifacts;
	}
	
	@SuppressWarnings("deprecation")
	public Artifact asArtifact (MavenProject project, Dependency dependency) throws MavenExecutionException {
		DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
		coordinate.setGroupId(dependency.getGroupId());
		coordinate.setArtifactId(dependency.getArtifactId());
		coordinate.setVersion(dependency.getVersion());
		coordinate.setExtension(dependency.getType());
		coordinate.setClassifier(dependency.getClassifier());

		ArtifactResult ar;
		try {
			ar = artifactResolver.resolveArtifact(project.getProjectBuildingRequest(), coordinate);
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
	
	
}
