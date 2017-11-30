package org.em.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.beryx.textio.InputReader.ValueChecker;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commsen.em.storage.ContractStorage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import aQute.libg.tuple.Pair;

/**
 * Goal which ...
 */
@Mojo(name = "addContractors", requiresProject = false)
public class AddContractorsMojo extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(AddContractorsMojo.class);

	private TextIO textIO = TextIoFactory.getTextIO();

	@Requirement
	private ArtifactResolver artifactResolver;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	MavenProject project;

	@Component
	ContractStorage contractStorage;
	
	public void execute() throws MojoExecutionException {

		String gav = textIO.newStringInputReader().withDefaultValue("com.commsen.em.contractors")
				.withValueChecker(new ValueChecker<String>() {
					@Override
					public List<String> getErrorMessages(String val, String itemName) {
						if (val != null) {
							String[] vals = val.split(":");
							if (vals.length > 0 && vals.length <= 3) {
								return null;
							}
						}
						return Arrays.asList(
								"Please provide valid Maven coordinates. It could be 'group' or 'group:artifact' or 'group:artifact:version'!");
					}
				}).read("Maven group / group:artifact / group:artifact:version ");

		String[] vals = gav.split(":");
		String searchString = null;
		switch (vals.length) {
		case 1:
			searchString = "g:\"" + vals[0] + "\"+AND+p:\"jar\"";
			break;
		case 2:
			searchString = "g:\"" + vals[0] + "\"+AND+a:\"" + vals[1] + "\"+AND+p:\"jar\"";
			break;
		}

		List<Pair<String, URL>> artifacts = new LinkedList<>();

		try {
			if (vals.length < 3) {
				URL u = new URL(
						"http://search.maven.org/solrsearch/select?q=" + searchString + "&core=gav&rows=50&wt=json");
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(u.openStream(), StandardCharsets.UTF_8))) {

					Gson gson = new Gson();
					JsonReader jsonReader = gson.newJsonReader(br);
					JsonParser parser = new JsonParser();
					JsonObject jsonObj = (JsonObject) parser.parse(jsonReader);
					
					int resultCount = jsonObj.getAsJsonObject("response").getAsJsonPrimitive("numFound").getAsInt();
					if (resultCount > 100) {
						throw new MojoExecutionException(
								"Found " + resultCount + " matches! Please refine your search.");
					}
					if (resultCount > 20) {
						logger.warn("Found " + resultCount + " matches! It may take very long time to process them.");
					}
					
					JsonArray docs = jsonObj.getAsJsonObject("response").getAsJsonArray("docs");
					for (JsonElement jsonElement : docs) {
						JsonObject entry = jsonElement.getAsJsonObject();
						artifacts.add(toPair(entry.get("id").getAsString(), entry.get("g").getAsString(),
								entry.get("a").getAsString(), entry.get("v").getAsString()));
					}
				}
			} else {
				artifacts.add(toPair(gav, vals[0], vals[1], vals[2]));
			}

			File f = new File("/tmp/.em");
			f.mkdirs();
			f = new File(f, "tmp.jar");
			f.deleteOnExit();

			for (Pair<String, URL> pair : artifacts) {
				logger.debug("Downloading " + pair.getSecond());
				try (FileOutputStream fos = new FileOutputStream(f)) {
					ReadableByteChannel rbc = Channels.newChannel(pair.getSecond().openStream());
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					boolean added = contractStorage.saveContractor(f, pair.getFirst());
					logger.info((added ? "[√] " : "[X] ") + pair.getFirst());
				} catch (IOException e) {
					logger.warn("Error while processing " + pair.getFirst() + "!", e);
				}
			}

		} catch (IOException e) {
			throw new MojoExecutionException("Error while trying to register contractors!", e);
		}

	}

	private Pair<String, URL> toPair(String id, String group, String artifact, String version)
			throws MalformedURLException {
		URL url = new URL("http://repo1.maven.org/maven2/" + group.replace(".", "/") + "/" + artifact + "/" + version
				+ "/" + artifact + "-" + version + ".jar");
		return new Pair<String, URL>(id, url);
	}
}
