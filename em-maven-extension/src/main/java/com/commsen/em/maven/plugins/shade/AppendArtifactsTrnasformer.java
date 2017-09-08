package com.commsen.em.maven.plugins.shade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppendArtifactsTrnasformer implements ResourceTransformer {

	private static final Logger logger	= LoggerFactory.getLogger(AppendArtifactsTrnasformer.class);

	private File location;

	private boolean done = false;

	@Override
	public boolean canTransformResource(String resource) {
		return !done && "META-INF".equals(resource);
	}

	@Override
	public void processResource(String resource, InputStream is, List<Relocator> relocators) throws IOException {
		// do nothing
	}

	@Override
	public boolean hasTransformedResource() {
		return !done;
	}

	@Override
	public void modifyOutputStream(JarOutputStream jos) throws IOException {

		if (location == null) {
			logger.warn("Can not append artifacts! Artifacts location not provided!");
			return;
		}

		if (!location.exists() || !location.isDirectory()) {
			logger.warn("Can not append artifacts! `{}` is not a valid directory!", location);
			return;
		}

		File[] files = location.listFiles();
		if (files != null) {
			for (File file : files) {
				JarFile jarFile = new JarFile(file);
				jarFile.stream().forEach(entry -> {
					if (!entry.getName().startsWith("META-INF/MANIFEST.MF")) {
						try {
							jos.putNextEntry(new JarEntry(entry.getName()));
							IOUtil.copy(jarFile.getInputStream(entry), jos);
						} catch (IOException e) {
							if (e instanceof ZipException && e.getMessage().startsWith("duplicate entry")) {
								logger.debug("Skipped existing file " + entry.getName());
							} else {
								logger.warn("Could not copy file '" + entry.getName() + "' from '" + jarFile.getName() + "' to the generated jar", e);
							}
						}
					}
				});
				jarFile.close();
			}
		}

		done = true;
	}

}
