package com.commsen.em.maven.extension;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "em")
public class EccentricModularityMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	private Logger logger = LoggerFactory.getLogger(EccentricModularityMavenLifecycleParticipant.class);

	@Requirement(role = ExecutionListener.class, hint = "em")
	private EccentricModularityExecutionListener executionListener;

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

		Set<String> goals = new HashSet<>(session.getGoals());

		if (goals.size() == 1 && goals.contains("clean")) {
			logger.info("Eccentric modularity not started (clean only session)! ");
			return;
		}

		executionListener.setDelegate(session.getRequest().getExecutionListener());
		session.getRequest().setExecutionListener(executionListener);
		logger.info("Eccentric modularity extension started!");
	}

}
