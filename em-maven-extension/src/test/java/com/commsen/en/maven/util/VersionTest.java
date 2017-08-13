package com.commsen.en.maven.util;

import org.junit.Assert;
import org.junit.Test;

import com.commsen.em.maven.util.VersionUtil;

public class VersionTest {

	@Test
	public void semanticVersioning () {

		// more than 3 digits
		Assert.assertEquals("1.2.3.4", VersionUtil.sementicVersion("1.2.3.4"));
		Assert.assertEquals("1.2.3.4.5", VersionUtil.sementicVersion("1.2.3.4.5"));
		
		// 3 digits
		Assert.assertEquals("1.2.3", VersionUtil.sementicVersion("1.2.3"));
		Assert.assertEquals("1.2.3.RC1", VersionUtil.sementicVersion("1.2.3.RC1"));
		Assert.assertEquals("1.2.3.SNAPSHOT", VersionUtil.sementicVersion("1.2.3-SNAPSHOT"));
		Assert.assertEquals("1.2.3.a", VersionUtil.sementicVersion("1.2.3a"));
		Assert.assertEquals("1.2.3.a_b", VersionUtil.sementicVersion("1.2a.3b"));
		Assert.assertEquals("1.2.3.a_b_c", VersionUtil.sementicVersion("1a.2b.3c"));
		Assert.assertEquals("1.2.3.a_b_c_RC1", VersionUtil.sementicVersion("1a.2b.3c.RC1"));
		Assert.assertEquals("1.2.3.a_b_c_RC1-SNAPSHOT", VersionUtil.sementicVersion("1a.2b.3c.RC1-SNAPSHOT"));
		
		// 2 digits
		Assert.assertEquals("1.2.0", VersionUtil.sementicVersion("1.2"));
		Assert.assertEquals("1.2.0.RC1", VersionUtil.sementicVersion("1.2.RC1"));
		Assert.assertEquals("1.2.0.SNAPSHOT", VersionUtil.sementicVersion("1.2-SNAPSHOT"));
		Assert.assertEquals("1.2.0.a", VersionUtil.sementicVersion("1.2a"));
		Assert.assertEquals("1.2.0.a_b", VersionUtil.sementicVersion("1a.2b"));
		Assert.assertEquals("1.2.0.a_b_RC1", VersionUtil.sementicVersion("1a.2b.RC1"));
		Assert.assertEquals("1.2.0.a_b_RC1-SNAPSHOT", VersionUtil.sementicVersion("1a.2b.RC1-SNAPSHOT"));

		// 1 digit
		Assert.assertEquals("1.0.0", VersionUtil.sementicVersion("1"));
		Assert.assertEquals("1.0.0.RC1", VersionUtil.sementicVersion("1.RC1"));
		Assert.assertEquals("1.0.0.SNAPSHOT", VersionUtil.sementicVersion("1-SNAPSHOT"));
		Assert.assertEquals("1.0.0.a", VersionUtil.sementicVersion("1a"));
		Assert.assertEquals("1.0.0.a_RC1", VersionUtil.sementicVersion("1a.RC1"));
		Assert.assertEquals("1.0.0.a_RC1-SNAPSHOT", VersionUtil.sementicVersion("1a.RC1-SNAPSHOT"));

		// no digit
		Assert.assertEquals("0.0.0", VersionUtil.sementicVersion(null));
		Assert.assertEquals("0.0.0", VersionUtil.sementicVersion(""));
		Assert.assertEquals("0.0.0", VersionUtil.sementicVersion("   \t  "));
		Assert.assertEquals("0.0.0.A", VersionUtil.sementicVersion("A"));
		Assert.assertEquals("0.1.0.A", VersionUtil.sementicVersion("A.1"));
		Assert.assertEquals("1.0.1.A", VersionUtil.sementicVersion("1.A.1"));
		Assert.assertEquals("0.0.0.A", VersionUtil.sementicVersion(" .A. "));
		Assert.assertEquals("1.0.1", VersionUtil.sementicVersion("1. .1"));
		Assert.assertEquals("0.0.0.null", VersionUtil.sementicVersion("null"));
		Assert.assertEquals("0.0.0", VersionUtil.sementicVersion("."));
		Assert.assertEquals("0.0.0", VersionUtil.sementicVersion("....."));

		// other 
		Assert.assertEquals("0.1.0.gl1-android-2_1_r1", VersionUtil.sementicVersion("gl1.1-android-2.1_r1"));
		
		
		
	}

}
