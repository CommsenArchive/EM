package com.commsen.em.annotation.processors;

import java.lang.reflect.Field;

import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;

public class ContractBuilder {

	public static class RequiresBuilder {

		private Requires annotation;

		public RequiresBuilder() {
		}

		public RequiresBuilder add(String name, String value)
				throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
			Field f = this.getClass().getDeclaredField(name);
			f.set(this, value);
			return this;
		}

		public RequiresBuilder from(Requires requires) {
			annotation = requires;
			return this;
		}

		public String build() {

			if (annotation.raw() != null && !annotation.raw().trim().isEmpty()) {
				return annotation.raw();
			}

			StringBuilder sb = new StringBuilder(annotation.ns());
			if (annotation.filter() != null && !annotation.filter().trim().isEmpty()) {
				sb.append(";filter:='").append(annotation.filter()).append("'");
			} else if (annotation.value() != null && !annotation.value().trim().isEmpty()) {
				sb.append(";filter:='(").append(annotation.ns()).append("=").append(annotation.value()).append(")'");
			}
			if (annotation.effective() != null && !annotation.effective().trim().isEmpty())
				sb.append(";effective:='").append(annotation.effective()).append("'");
			
			if (annotation.resolution() != null && !annotation.resolution().trim().isEmpty())
				sb.append(";resolution:='").append(annotation.resolution()).append("'");

			if (annotation.extra() != null && !annotation.extra().trim().isEmpty())
				sb.append(";").append(annotation.extra());

			return sb.toString();
		}
	}
	
	public static class ProvidesBuilder {

		private Provides annotation;
		
		
		public ProvidesBuilder() {
		}

		
		public ProvidesBuilder from(Provides provides) {
			annotation = provides;
			return this;
		}

		public String build() {

			if (annotation.raw() != null && !annotation.raw().trim().isEmpty()) {
				return annotation.raw();
			}

			StringBuilder sb = new StringBuilder(annotation.ns());
			if (annotation.value() != null && !annotation.value().trim().isEmpty())
				sb.append(";").append(annotation.ns()).append("='").append(annotation.value()).append("'");
			if (annotation.options() != null && annotation.options().length > 0)
				sb.append(";").append(String.join(";", annotation.options()));
			if (annotation.uses() != null && annotation.uses().length > 0)
				sb.append(";").append("uses:='").append(String.join(",", annotation.uses())).append("'");
			if (annotation.mandatory() != null && annotation.mandatory().length > 0)
				sb.append(";").append("mandatory:='").append(String.join(",", annotation.mandatory())).append("'");
			if (annotation.version() != null && !annotation.version().trim().isEmpty())
				sb.append(";").append("version:Version='").append(annotation.version()).append("'");
			if (annotation.extra() != null && !annotation.extra().trim().isEmpty())
				sb.append(";").append(annotation.extra());
			if (annotation.effective() != null && !annotation.effective().trim().isEmpty())
				sb.append(";effective:='").append(annotation.effective()).append("'");

			return sb.toString();
		}
	}
}
