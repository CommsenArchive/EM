package com.commsen.em.maven.util;

import aQute.libg.tuple.Pair;

public class Version {
	
	
	public static String semantic (String version) {
		
		if (version == null || version.trim().isEmpty()) return "0.0.0";

		int dashIndex = version.indexOf("-");
		String main = version;
		String rest = "";
		if (dashIndex >= 0) {
			main = version.substring(0, dashIndex);
			rest = version.substring(dashIndex+1);
		}

		String[] groups = main.split("\\.");
		
		StringBuilder result = new StringBuilder();
		StringBuilder additional = new StringBuilder();

		int semGroups = 0;
		for (int i = 0; i < groups.length; i++) {
			Pair<Integer, String> pair = digitalize(groups[i]);
			if (pair.getFirst() > 0 || semGroups < 3) {
				if (semGroups != 0) {
					result.append(".");
				}
				result.append(pair.getFirst());
				semGroups++;
			}
			if (!pair.getSecond().isEmpty()) {
				if (additional.length() > 0 ) additional.append("_");
				additional.append(pair.getSecond().replaceAll("\\.", "_"));
			}
		}
		
		if (semGroups == 0) {
			result.append("0.0.0");
		} else {
			while (semGroups++ < 3) {
				result.append(".0");
			}
		}
		
		String delimiter = ".";
		if (additional.length() > 0) {
			result.append(delimiter).append(additional);
			delimiter = "-";
		}

		if (rest.length() > 0) {
			result.append(delimiter).append(rest.replaceAll("\\.", "_"));
		}
		
		return result.toString();
	}

	public static Pair<Integer, String> digitalize (String string) {
		
		int i = 0;
		while (i < string.length() && Character.isDigit(string.charAt(i))) i++;
		
		if (i == 0) {
			return  new Pair<Integer, String>(0, string.trim());
		}
		
		Integer number = Integer.parseInt(string.substring(0, i));
		String rest = string.substring(i).trim();
		
		return  new Pair<Integer, String>(number, rest);
	}
	
	
}
