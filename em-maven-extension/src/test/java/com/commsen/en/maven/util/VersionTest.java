package com.commsen.en.maven.util;

import org.junit.Assert;
import org.junit.Test;

import com.commsen.em.maven.util.Version;

public class VersionTest {

	@Test
	public void semanticVersioning () {

		// more than 3 digits
		Assert.assertEquals("1.2.3.4", Version.semantic("1.2.3.4"));
		Assert.assertEquals("1.2.3.4.5", Version.semantic("1.2.3.4.5"));
		
		// 3 digits
		Assert.assertEquals("1.2.3", Version.semantic("1.2.3"));
		Assert.assertEquals("1.2.3.RC1", Version.semantic("1.2.3.RC1"));
		Assert.assertEquals("1.2.3.SNAPSHOT", Version.semantic("1.2.3-SNAPSHOT"));
		Assert.assertEquals("1.2.3.a", Version.semantic("1.2.3a"));
		Assert.assertEquals("1.2.3.a_b", Version.semantic("1.2a.3b"));
		Assert.assertEquals("1.2.3.a_b_c", Version.semantic("1a.2b.3c"));
		Assert.assertEquals("1.2.3.a_b_c_RC1", Version.semantic("1a.2b.3c.RC1"));
		Assert.assertEquals("1.2.3.a_b_c_RC1-SNAPSHOT", Version.semantic("1a.2b.3c.RC1-SNAPSHOT"));
		
		// 2 digits
		Assert.assertEquals("1.2.0", Version.semantic("1.2"));
		Assert.assertEquals("1.2.0.RC1", Version.semantic("1.2.RC1"));
		Assert.assertEquals("1.2.0.SNAPSHOT", Version.semantic("1.2-SNAPSHOT"));
		Assert.assertEquals("1.2.0.a", Version.semantic("1.2a"));
		Assert.assertEquals("1.2.0.a_b", Version.semantic("1a.2b"));
		Assert.assertEquals("1.2.0.a_b_RC1", Version.semantic("1a.2b.RC1"));
		Assert.assertEquals("1.2.0.a_b_RC1-SNAPSHOT", Version.semantic("1a.2b.RC1-SNAPSHOT"));

		// 1 digit
		Assert.assertEquals("1.0.0", Version.semantic("1"));
		Assert.assertEquals("1.0.0.RC1", Version.semantic("1.RC1"));
		Assert.assertEquals("1.0.0.SNAPSHOT", Version.semantic("1-SNAPSHOT"));
		Assert.assertEquals("1.0.0.a", Version.semantic("1a"));
		Assert.assertEquals("1.0.0.a_RC1", Version.semantic("1a.RC1"));
		Assert.assertEquals("1.0.0.a_RC1-SNAPSHOT", Version.semantic("1a.RC1-SNAPSHOT"));

		// no digit
		Assert.assertEquals("0.0.0", Version.semantic(null));
		Assert.assertEquals("0.0.0", Version.semantic(""));
		Assert.assertEquals("0.0.0", Version.semantic("   \t  "));
		Assert.assertEquals("0.0.0.A", Version.semantic("A"));
		Assert.assertEquals("0.1.0.A", Version.semantic("A.1"));
		Assert.assertEquals("1.0.1.A", Version.semantic("1.A.1"));
		Assert.assertEquals("0.0.0.A", Version.semantic(" .A. "));
		Assert.assertEquals("1.0.1", Version.semantic("1. .1"));
		Assert.assertEquals("0.0.0.null", Version.semantic("null"));
		Assert.assertEquals("0.0.0", Version.semantic("."));
		Assert.assertEquals("0.0.0", Version.semantic("....."));

		// other 
		Assert.assertEquals("0.1.0.gl1-android-2_1_r1", Version.semantic("gl1.1-android-2.1_r1"));
		
		
		
	}

}
