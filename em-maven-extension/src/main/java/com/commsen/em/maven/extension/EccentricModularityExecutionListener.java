package com.commsen.em.maven.extension;

import static com.commsen.em.maven.extension.Constants.PROP_ACTION_AUGMENT;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_DEPLOY;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_EXECUTABLE;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_METADATA;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_RESOLVE;
import static com.commsen.em.maven.extension.Constants.PROP_PREFIX;
import static com.commsen.em.maven.extension.Constants.VAL_BND_VERSION;
import static com.commsen.em.maven.extension.Constants.VAL_INDEX_TYPE;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.plugins.BndExportPlugin;
import com.commsen.em.maven.plugins.BndIndexerPlugin;
import com.commsen.em.maven.plugins.BndPlugin;
import com.commsen.em.maven.plugins.DistroPlugin;

@Component(role = ExecutionListener.class, hint = "em")
public class EccentricModularityExecutionListener extends AbstractExecutionListener {

	@Requirement
	private BndPlugin bndPlugin;

	@Requirement
	private BndIndexerPlugin bndIndexerPlugin;

	@Requirement
	private BndExportPlugin bndExportPlugin;

	@Requirement
	private DistroPlugin distroPlugin;
	
	@Requirement(role = ArtifactRepositoryLayout.class, hint = "default")
	private ArtifactRepositoryLayout defaultLayout;

	private ExecutionListener delegate;
	
	private Logger logger = LoggerFactory.getLogger(EccentricModularityExecutionListener.class);

	public void setDelegate(ExecutionListener executionListener) {
		this.delegate = executionListener;
	}

	@Override
	public void projectStarted(ExecutionEvent event) {
		delegate.projectStarted(event);

		MavenProject project = event.getProject();
		
		if (VAL_BND_VERSION.toLowerCase().contains("snapshot")) {
			addBndSnapshotRepo(project);
		}

		boolean indexBundles = project.getProperties().containsKey(Constants.PROP_CONFIG_INDEX);
		boolean actionFound = false;

		/*
		 * TODO: figure out what to do if more than one action is provided! 
		 * For now all will be executed which may have weird results 
		 */
		logger.info("Adding plugins and adapting project configuration based on provided '" + PROP_PREFIX + "*' propeties!");

		if (project.getProperties().containsKey(PROP_ACTION_METADATA)) {
			actionFound = true;
			try {
				bndPlugin.addToBuild(project);
			} catch (MavenExecutionException e) {
				throw new RuntimeException("Failed to add bnd-maven-plugin!", e);
			}
		}

		if (project.getProperties().containsKey(PROP_ACTION_AUGMENT)) {
			actionFound = true;
			try {
				bndPlugin.addToBuildForAugment(project);
			} catch (MavenExecutionException e) {
				throw new RuntimeException("Failed to add bnd-maven-plugin!", e);
			}

		}

		if (VAL_INDEX_TYPE.equals(project.getPackaging())) {
			actionFound = true;
			/*
			 * BND indexer plug-in is already added in the custom lifecycle for
			 * "index" type, so we only need to configure it
			 */
			try {
				bndIndexerPlugin.configureForIndexGeneration(project);
			} catch (MavenExecutionException e) {
				throw new RuntimeException("Failed to configure bnd-indexer-maven-plugin!", e);
			}
		}

		if (project.getProperties().containsKey(PROP_ACTION_RESOLVE)) {
			actionFound = true;			
			try {
				/*
				 * TODO: Need a better way to add multiple plugins! For now add
				 * plugins in reverse order since they are added to the
				 * beginning of the list
				 */
				bndExportPlugin.addToPomForExport(project);
				if (indexBundles) bndIndexerPlugin.addToPomForIndexingTmpBundles(project);
				bndPlugin.addToBuild(project);
			} catch (MavenExecutionException e) {
				throw new RuntimeException("Failed to add one of the required bnd plugins!", e);
			}
		}

		if (project.getProperties().containsKey(PROP_ACTION_EXECUTABLE)) {
			actionFound = true;
			try {
				/*
				 * TODO: Need a better way to add multiple plugins! For now add
				 * plugins in reverse order since they are added to the
				 * beginning of the list
				 */
				bndExportPlugin.addToPomForExecutable(project);
				if (indexBundles) bndIndexerPlugin.addToPomForIndexingTmpBundles(project);
				bndPlugin.addToBuild(project);
			} catch (MavenExecutionException e) {
				throw new RuntimeException("Failed to add one of the required bnd plugins!", e);
			}
		}

		if (project.getProperties().containsKey(PROP_ACTION_DEPLOY)) {
			actionFound = true;
			String runtime = project.getProperties().getProperty(PROP_ACTION_DEPLOY, "");
			try {
				distroPlugin.createDistroJar(project, runtime);
			} catch (MavenExecutionException e) {
				throw new RuntimeException("Failed to extract metadata from the target runtime!", e);
			}
			try {
				/*
				 * TODO: Need a better way to add multiple plugins! For now add
				 * plugins in reverse order since they are added to the
				 * beginning of the list
				 */
				bndExportPlugin.addToPomForExport(project);
				if (indexBundles) bndIndexerPlugin.addToPomForIndexingTmpBundles(project);
				bndPlugin.addToBuild(project);
			} catch (MavenExecutionException e) {
				throw new RuntimeException("Failed to add one of the required bnd plugins!", e);
			}
		}
		
		if (!actionFound) {
			logger.info("No '" + PROP_PREFIX + "*' action found! Project will be executed AS IS!");
		}
	}


	@Override
	public void projectDiscoveryStarted(ExecutionEvent event) {
		delegate.projectDiscoveryStarted(event);
	}

	@Override
	public void sessionStarted(ExecutionEvent event) {

		delegate.sessionStarted(event);
	}

	@Override
	public void sessionEnded(ExecutionEvent event) {

		delegate.sessionEnded(event);
	}

	@Override
	public void projectSkipped(ExecutionEvent event) {

		delegate.projectSkipped(event);
	}

	@Override
	public void projectSucceeded(ExecutionEvent event) {

		delegate.projectSucceeded(event);
		bndExportPlugin.cleanup();
	}

	@Override
	public void projectFailed(ExecutionEvent event) {

		delegate.projectFailed(event);
		bndExportPlugin.cleanup();
	}

	@Override
	public void forkStarted(ExecutionEvent event) {

		delegate.forkStarted(event);
	}

	@Override
	public void forkSucceeded(ExecutionEvent event) {

		delegate.forkSucceeded(event);
	}

	@Override
	public void forkFailed(ExecutionEvent event) {

		delegate.forkFailed(event);
	}

	@Override
	public void mojoSkipped(ExecutionEvent event) {

		delegate.mojoSkipped(event);
	}

	@Override
	public void mojoStarted(ExecutionEvent event) {

		delegate.mojoStarted(event);
	}

	@Override
	public void mojoSucceeded(ExecutionEvent event) {

		delegate.mojoSucceeded(event);
	}

	@Override
	public void mojoFailed(ExecutionEvent event) {

		delegate.mojoFailed(event);
	}

	@Override
	public void forkedProjectStarted(ExecutionEvent event) {

		delegate.forkedProjectStarted(event);
	}

	@Override
	public void forkedProjectSucceeded(ExecutionEvent event) {

		delegate.forkedProjectSucceeded(event);
	}

	@Override
	public void forkedProjectFailed(ExecutionEvent event) {

		delegate.forkedProjectFailed(event);
	}

	private void addBndSnapshotRepo(MavenProject project) {

		ArtifactRepository ar = new MavenArtifactRepository();
		ar.setId("bnd-snapshots");
		ar.setUrl("https://bndtools.ci.cloudbees.com/job/bnd.master/lastSuccessfulBuild/artifact/dist/bundles/");
		ar.setLayout(defaultLayout);

		List<ArtifactRepository> pluginRepos = new LinkedList<>();
		pluginRepos.addAll(project.getPluginArtifactRepositories());
		pluginRepos.add(ar);
		project.setPluginArtifactRepositories(pluginRepos);

		List<ArtifactRepository> repos = new LinkedList<>();
		repos.addAll(project.getRemoteArtifactRepositories());
		repos.add(ar);
		project.setRemoteArtifactRepositories(repos);

	}
	

}
