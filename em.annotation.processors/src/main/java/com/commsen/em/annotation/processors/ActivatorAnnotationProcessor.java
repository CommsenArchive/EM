package com.commsen.em.annotation.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.commsen.em.annotations.Activator;

@SupportedAnnotationTypes("com.commsen.em.annotations.Activator")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ActivatorAnnotationProcessor extends AbstractProcessor {

	private Messager messager;
	private Filer filer;

	private String activator;
	private String extensionActivator;

	public ActivatorAnnotationProcessor() {
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		this.messager = processingEnv.getMessager();
		this.filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!roundEnv.errorRaised() && !roundEnv.processingOver()) {
			processRound(annotations, roundEnv);
		}
		return false;
	}

	private void processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		loop: for (TypeElement annotation : annotations) {
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				if (element.getAnnotation(Activator.class).extension()) {
					extensionActivator = element.asType().toString();
				} else {
					activator = element.asType().toString();
				}
				if (activator != null && extensionActivator != null) {
					break loop;
				}
			}
		}
		try {
			generateFile();
		} catch (IOException e) {
			messager.printMessage(Kind.ERROR, "IOException while generating file with contracts! " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void generateFile() throws IOException {

		FileObject fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "em.activator.bnd");
		try (Writer writer = fileObject.openWriter()) {
			if (activator != null) {
				writer.write("Bundle-Activator: " + activator + "\n");
			}
			if (extensionActivator != null) {
				writer.write("ExtensionBundle-Activator: " + extensionActivator + "\n");
			}
		}
	}

}
