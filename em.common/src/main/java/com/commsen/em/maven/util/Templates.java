package com.commsen.em.maven.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.plexus.component.annotations.Component;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Component(role = Templates.class)
public class Templates {

	private Configuration cfg = new Configuration();
	
	public Templates() {
		cfg.setClassForTemplateLoading(this.getClass(), "/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	public Template get (String template) throws IOException {
		return cfg.getTemplate(template);
	}

	public void process (String template, Object dataModel, Writer out) throws IOException, TemplateException {
		get(template).process(dataModel, out);
	}
	
	public String process (String template, Object dataModel) throws IOException, TemplateException {
		StringWriter out = new StringWriter();
		get(template).process(dataModel, out);
		return out.toString();
	}
	
}
