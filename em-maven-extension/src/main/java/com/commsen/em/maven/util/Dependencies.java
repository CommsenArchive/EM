package com.commsen.em.maven.util;

import java.io.IOException;
import java.util.Collection;
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
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
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

	public Set<Artifact> managedAsArtifacts(MavenProject project) throws MavenExecutionException {
		if (project.getDependencyManagement() != null) {
			return asArtifacts(project, project.getDependencyManagement().getDependencies());
		} else {
			return Collections.emptySet();
		}
	}

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

	public Set<Artifact> directDependencies(MavenProject project, Collection<Dependency> initial) throws MavenExecutionException {
		ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest();
		artifactResolutionRequest.setResolveTransitively(true);
		artifactResolutionRequest.setRemoteRepositories(project.getRemoteArtifactRepositories());
		artifactResolutionRequest.setLocalRepository(project.getProjectBuildingRequest().getLocalRepository());
		Set<Artifact> artifacts = new HashSet<>();
		for (Dependency dependency : initial) {
			Artifact artifact = asArtifact(project, dependency);
			artifactResolutionRequest.setArtifact(artifact);

			ArtifactResolutionResult artifactResolutionResult = mavenRepoSystem.resolve(artifactResolutionRequest);
			artifactResolutionResult.getArtifacts().stream()
				.peek(d -> { if (Flag.verbose()) logger.info("Processing '{}' from '{}'", d,  toId(dependency)); })
				.filter(d -> "jar".equals(d.getType()))
				.filter(d -> "compile".equals(d.getScope()) || "provided".equals(d.getScope()) || "runtime".equals(d.getScope()))
				.forEach(d -> artifacts.add(d));
		}
		return artifacts;
	}
	
	@SuppressWarnings("deprecation")
	public Set<Artifact> asArtifacts(MavenProject project, Collection<Dependency> initial) throws MavenExecutionException {

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

			System.out.println("Processing: " + dependency);
			
			Set<Artifact> tmpArtifacts = new HashSet<>();
			
			
			// make sure to not process excluded dependencies
			Set<String> excludes = new HashSet<>();
			for (Exclusion exclusion : dependency.getExclusions()) {
				if (exclusion.getGroupId() != null) {
					excludes.add(exclusion.getGroupId() + ":" + exclusion.getArtifactId());
				} else {
					excludes.add(exclusion.getArtifactId());
				}
			} 
			ArtifactFilter filter = new ExclusionSetFilter(excludes);
			
			Artifact artifact = asArtifact(project, dependency);
			artifacts.add(artifact);
			artifactResolutionRequest.setArtifact(artifact);
			artifactResolutionRequest.setCollectionFilter(filter);
			ArtifactResolutionResult artifactResolutionResult = mavenRepoSystem.resolve(artifactResolutionRequest);

			artifactResolutionResult.getArtifacts().stream()
				.peek(d -> { if (Flag.verbose()) logger.info("Processing '{}' from '{}'", d,  toId(dependency)); })
				.filter(d -> "jar".equals(d.getType()))
				.filter(d -> "compile".equals(d.getScope()) || "provided".equals(d.getScope()) || "runtime".equals(d.getScope()))
				.forEach(d -> tmpArtifacts.add(d));
			
			tmpArtifacts.stream().sorted().forEach( a -> System.out.println("   Found: " + a));
			
			artifacts.addAll(tmpArtifacts);
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
	

	public void addToDependencyManagement(MavenProject project, Dependency dependency) {
		if (project.getModel().getDependencyManagement() == null) {
			project.getModel().setDependencyManagement(new DependencyManagement());
		}
		project.getModel().getDependencyManagement().addDependency(dependency);;
	}

	
	private String toId (Dependency dependency) {
		return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
	}
	
	
}
