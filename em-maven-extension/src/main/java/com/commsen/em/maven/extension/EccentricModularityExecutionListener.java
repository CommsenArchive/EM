package com.commsen.em.maven.extension;

import static com.commsen.em.maven.extension.Constants.PROP_ACTION_AUGMENT;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_EXECUTABLE_OSGI;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_METADATA;
import static com.commsen.em.maven.extension.Constants.PROP_ACTION_RESOLVE;
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

@Component(role = ExecutionListener.class, hint = "em")
public class EccentricModularityExecutionListener extends AbstractExecutionListener {

	@Requirement
	private BndPlugin bndPlugin;

	@Requirement
	private BndIndexerPlugin bndIndexerPlugin;

	@Requirement
	private BndExportPlugin bndExportPlugin;

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

		addBndSnapshotRepo(project);

		if (project.getProperties().containsKey(PROP_ACTION_METADATA)) {
			logger.info("Adding bnd-maven-plugin to the project to generate metadata and add it to MANIFEST.MF file!");
			try {
				bndPlugin.addToBuild(project);
			} catch (MavenExecutionException e) {
				logger.error("Failed to add bnd-maven-plugin!", e);
				event.getSession().getResult().addException(e);
			}
		}

		if (project.getProperties().containsKey(PROP_ACTION_AUGMENT)) {
			logger.info(
					"Adding bnd-maven-plugin to the project to genrate jar that augments (provides metadata for) other jars!");
			try {
				bndPlugin.addToBuildForAugment(project);
			} catch (MavenExecutionException e) {
				logger.error("Failed to add bnd-maven-plugin!", e);
				event.getSession().getResult().addException(e);
			}

		}

		if (VAL_INDEX_TYPE.equals(project.getPackaging())) {
			logger.info("Configuring bnd-indexer-maven-plugin to genrate an index from dependencies!");

			/*
			 * BND indexer plug-in is already added in the custom lifecycle for
			 * "index" type, so we only need to configure it
			 */
			try {
				bndIndexerPlugin.configureForIndexGeneration(project);
			} catch (MavenExecutionException e) {
				logger.error("Failed to configure bnd-indexer-maven-plugin!", e);
				event.getSession().getResult().addException(e);
			}
		}

		if (project.getProperties().containsKey(PROP_ACTION_RESOLVE)) {
			logger.info(" Adding the following plugins:\n" //
					+ "  bnd-maven-plugin \n" //
					+ "  bnd-indexer-maven-plugin \n" //
					+ "  bnd-export-maven-plugin \n" //
					+ " configured to resolve and export dependencies from indexes");

			try {
				/*
				 * TODO: Need a better way to add multiple plugins! For now add
				 * plugins in reverse order since they are added to the
				 * beginning of the list
				 */
				bndExportPlugin.addToPomForExport(project);
				bndIndexerPlugin.addToPom(project);
				bndPlugin.addToBuild(project);
			} catch (MavenExecutionException e) {
				logger.error("Failed to add one of the required bnd plugins!", e);
				event.getSession().getResult().addException(e);
			}
		}

		if (project.getProperties().containsKey(PROP_ACTION_EXECUTABLE_OSGI)) {
			logger.info(" Adding the following plugins:\n" //
					+ "  bnd-maven-plugin \n" //
					+ "  bnd-indexer-maven-plugin \n" //
					+ "  bnd-export-maven-plugin \n" //
					+ " configured to create standalone executable");

			try {
				/*
				 * TODO: Need a better way to add multiple plugins! For now add
				 * plugins in reverse order since they are added to the
				 * beginning of the list
				 */
				bndExportPlugin.addToPomForExecutable(project);
				bndIndexerPlugin.addToPom(project);
				bndPlugin.addToBuild(project);
			} catch (MavenExecutionException e) {
				logger.error("Failed to add one of the required bnd plugins!", e);
				event.getSession().getResult().addException(e);
			}
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
	
	private void addBndSnapshotRepo(MavenProject project)  {

		ArtifactRepository ar = new  MavenArtifactRepository();
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
