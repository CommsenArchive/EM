package com.commsen.em.maven.util;

import java.io.StringReader;

import org.apache.maven.MavenExecutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

@Component(role = MavenConfigUtil.class)
public class MavenConfigUtil {

	public Xpp3Dom config2dom(String config) throws MavenExecutionException {
		try {
			return Xpp3DomBuilder.build(new StringReader(config));
		} catch (Exception e) {
			throw new MavenExecutionException("Error parsing config", e);
		}
	}

}
