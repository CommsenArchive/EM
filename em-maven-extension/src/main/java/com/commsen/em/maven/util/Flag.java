package com.commsen.em.maven.util;

public class Flag {

	public static boolean keepBndrun () {
		return System.getProperty("em.keepBndrun") != null;
	}

	public static boolean verbose () {
		return System.getProperty("em.verbose") != null;
	}
}
