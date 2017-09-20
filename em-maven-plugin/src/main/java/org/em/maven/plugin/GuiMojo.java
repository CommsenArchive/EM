package org.em.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import aQute.launcher.Launcher;

@Mojo(name = "gui", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresProject = false)
public class GuiMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Launcher.main(new String[] {});
		
//		if (Desktop.isDesktopSupported()) {
//			try {
//				Desktop.getDesktop().browse(new URI("http://www.example.com"));
//			} catch (IOException | URISyntaxException e) {
//				throw new MojoExecutionException ("Couldn't start default browser!", e);
//			}
//		}
	}

}
