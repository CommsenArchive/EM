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

import com.commsen.em.annotations.RuntimeExtension;

@SupportedAnnotationTypes("com.commsen.em.annotations.RuntimeExtension")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FragmentAnnotationProcessor extends AbstractProcessor {

	private Messager messager;
	private Filer filer;

	private String entry;

	public FragmentAnnotationProcessor() {
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
				switch (element.getAnnotation(RuntimeExtension.class).value()) {
				case FRAMEWORK:
					entry = "Fragment-Host: system.bundle; extension:=framework";
					break loop;
				case BOOTCLASSPATH:
					entry = "Fragment-Host: system.bundle; extension:=bootclasspath";
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

		FileObject fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "em.fragment.bnd");
		try (Writer writer = fileObject.openWriter()) {
			if (entry != null && !entry.trim().isEmpty()) {
				writer.write(entry + "\n");
				writer.write("Import-Package: !*");
			}
		}
	}

}
