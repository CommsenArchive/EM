package com.commsen.em.maven.plugins;

import static com.commsen.em.maven.extension.Constants.DEFAULT_DISTROS_FOLDER;
import static com.commsen.em.maven.extension.Constants.INTERNAL_DISTRO_FILE;
import static com.commsen.em.maven.extension.Constants.PROP_CONFIG_DISTRO_FOLDER;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.login.ConfigurationSpi;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.maven.extension.Constants;

import aQute.bnd.main.bnd;
import aQute.configurable.Config;

@Component(role = DistroPlugin.class)
public class DistroPlugin {

	private Logger logger = LoggerFactory.getLogger(DistroPlugin.class);


	public void createDistroJar(MavenProject project, String remote) throws MavenExecutionException {

		String distrosFolder = project.getProperties().getProperty(PROP_CONFIG_DISTRO_FOLDER, DEFAULT_DISTROS_FOLDER);

		bnd bnd = new bnd();
		try {
			String[] params = remote.split(":");
			if (params.length != 4) {
				throw new MavenExecutionException("Invalid remote. The format is `host:port:name:version` ",
						project.getFile());
			}

			File output = new File(project.getBasedir(), distrosFolder);

			if (output.exists()) {
				if (!output.isDirectory()) {
					throw new MavenExecutionException(
							"Need to create folder " + output + " but file with that name already exists!",
							project.getFile());
				}
			} else {
				if (output.mkdirs()) {
					logger.info("Created {} folder", output);
				} else {
					throw new MavenExecutionException("Failed to create folder " + output, project.getFile());
				}
			}

			File distroFile = new File(output, params[2] + "-" + params[3] + ".jar");

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

	public void clean(MavenProject project) throws MavenExecutionException, IOException {
		String distrosFolder = project.getProperties().getProperty(PROP_CONFIG_DISTRO_FOLDER, DEFAULT_DISTROS_FOLDER);
		File f = new File(project.getBasedir(), distrosFolder);
		if (f.exists() && f.isDirectory()) {
			logger.info("Deleting " + f);
			Files.walk(f.toPath()) //
					.map(Path::toFile) //
					.sorted((o1, o2) -> -o1.compareTo(o2)) //
					.forEach(File::delete); //
		}
	}

}
