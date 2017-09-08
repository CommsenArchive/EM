package com.commsen.em.maven.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class Templates {

	private static Templates instance;

	private Configuration cfg = new Configuration();
	
	private Templates() {
		cfg.setClassForTemplateLoading(this.getClass(), "/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	private static Templates get () {
		if (instance == null) instance = new Templates();
		return instance;
	}
 	
	public static Template get (String template) throws IOException {
		return get().cfg.getTemplate(template);
	}

	public static void process (String template, Object dataModel, Writer out) throws IOException, TemplateException {
		get(template).process(dataModel, out);
	}
	
	public static String process (String template, Object dataModel) throws IOException, TemplateException {
		StringWriter out = new StringWriter();
		get(template).process(dataModel, out);
		return out.toString();
	}
	
}
