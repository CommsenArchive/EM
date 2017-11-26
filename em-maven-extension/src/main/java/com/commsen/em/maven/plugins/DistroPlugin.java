package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.util.Constants.INTERNAL_DISTRO_FILE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.util.Constants;

import aQute.bnd.main.bnd;

@Component(role = DistroPlugin.class)
public class DistroPlugin {

	private Logger logger = LoggerFactory.getLogger(DistroPlugin.class);


	public void createDistroJar(MavenProject project, String remote) throws MavenExecutionException {

		Path emProjectDistro;
		try {
			emProjectDistro = Constants.getDistroFolder(project);
		} catch (IOException e) {
			throw new MavenExecutionException("Failed to create distro folder!", e);
		}

		bnd bnd = new bnd();
		try {
			String[] params = remote.split(":");
			if (params.length != 4) {
				throw new MavenExecutionException("Invalid remote. The format is `host:port:name:version` ",
						project.getFile());
			}

			File distroFile = emProjectDistro.resolve("-" + params[3] + ".jar").toFile();

			List<String> args = new LinkedList<String>();
			args.add("remote");
			args.add("-h");
			args.add(params[0]);
			args.add("-p");
			args.add(params[1]);
			args.add("distro");
			args.add("-o");
			args.add(distroFile.getAbsolutePath());
			args.add(params[2]);
			args.add(params[3]);

			logger.info("  - Building a distro jar (matadata only) for provided target runtime " + remote);
			logger.debug("    Running bnd with the following args : " + args);
			try {
				/*
				 *  TODO Figure out how to provide a connection timeout (hangs forever with wrong host/port) 
				 *  Perhaps run in separate thread
				 */
				bnd.start(args.toArray(new String[args.size()]));
			} catch (Exception e) {
				throw new MavenExecutionException("Failed to build distro jar", e);
			}
			logger.info("  - Saved distro jar in " + distroFile);

			project.getProperties().setProperty(INTERNAL_DISTRO_FILE, distroFile.getAbsolutePath());

		} finally {
			try {
				bnd.close();
			} catch (IOException e) {
				logger.error("Failed to properly close bnd!", e);
			}
		}
	}


}
