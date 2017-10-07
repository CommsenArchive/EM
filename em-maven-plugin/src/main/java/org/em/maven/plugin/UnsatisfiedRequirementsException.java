package org.em.maven.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnsatisfiedRequirementsException extends Exception {

	private static final long serialVersionUID = 1L;

	Map<String, Set<String>> unsatisfiedRequirements = new HashMap<>();

	private UnsatisfiedRequirementsException() {
	}

	@Override
	public String getMessage() {
		StringBuilder mainMessage = new StringBuilder("Unsatisfied requirements: \n");
		StringBuilder noContractors = new StringBuilder();
		for (String requirement : unsatisfiedRequirements.keySet()) {
			Set<String> contractors = unsatisfiedRequirements.get(requirement);
			if (contractors != null && !contractors.isEmpty()) {
				mainMessage.append("REQUIREMENT: " + requirement + "\n");
				mainMessage.append("HINT: consider adding one or more of the following contractors to '<em:contractors> property in your POM:\n");
				for (String contractor : contractors) {
					mainMessage.append("\t " + contractor + "\n");
				}
			} else {
				noContractors.append("REQUIREMENT: " + requirement + "\n");
			}
		}
		return mainMessage.append(noContractors).toString();
	}

	static class Builder {

		UnsatisfiedRequirementsException ex;

		public Builder() {
			ex = new UnsatisfiedRequirementsException();
		}

		public Builder add(String requirement) {
			if (!ex.unsatisfiedRequirements.containsKey(requirement)) {
				ex.unsatisfiedRequirements.put(requirement, Collections.emptySet());
			}
			return this;
		}

		public Builder add(String requirement, Set<String> contractors) {
			Set<String> existingContractors = ex.unsatisfiedRequirements.get(requirement);
			if (existingContractors == null) {
				existingContractors = new HashSet<>();
				ex.unsatisfiedRequirements.put(requirement, existingContractors);
			}
			existingContractors.addAll(contractors);
			return this;
		}

		public Builder add(String requirement, String contractor) {
			Set<String> existingContractors = ex.unsatisfiedRequirements.get(requirement);
			if (existingContractors == null) {
				existingContractors = new HashSet<>();
				ex.unsatisfiedRequirements.put(requirement, existingContractors);
			}
			existingContractors.add(contractor);
			return this;
		}

		public UnsatisfiedRequirementsException build() {
			return ex;
		}
	}
}
