package com.commsen.em.annotation.processors;

import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.commsen.em.annotation.processors.ContractBuilder.ProvidesBuilder;
import com.commsen.em.annotation.processors.ContractBuilder.RequiresBuilder;
import com.commsen.em.annotations.Provides;
import com.commsen.em.annotations.Requires;
import com.commsen.em.annotations.internal.ProvidesMany;
import com.commsen.em.annotations.internal.RequiresMany;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ContractsAnnotationProcessor extends AbstractProcessor {

	private Messager messager;
	private Elements elementUtils;
	private Filer filer;
	private Set<String> baseAnnotations = new HashSet<>();
	private Set<String> requrements = new HashSet<>();
	private Set<String> capabilities = new HashSet<>();

	private Stack<Map<? extends ExecutableElement, ? extends AnnotationValue>> stack = new Stack<>();

	public ContractsAnnotationProcessor() {
		baseAnnotations.add(Requires.class.getName());
		baseAnnotations.add(RequiresMany.class.getName());
		baseAnnotations.add(Provides.class.getName());
		baseAnnotations.add(ProvidesMany.class.getName());
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		this.messager = processingEnv.getMessager();
		this.elementUtils = processingEnv.getElementUtils();
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
		for (TypeElement annotation : annotations) {
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
//				System.out.println(" ===> Processing " + element);
				if (ANNOTATION_TYPE != element.getKind()) {
					processElement(element);
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

	private void processBaseAnnotatins(Element element) {
		Requires[] requires = element.getAnnotationsByType(Requires.class);
		for (Requires r : requires) {
			RequiresBuilder rb = new ContractBuilder.RequiresBuilder().from(r);
			requrements.add(processTemplate(element, rb.build()));
		}

		Provides[] provides = element.getAnnotationsByType(Provides.class);
		for (Provides p : provides) {
			ProvidesBuilder pb = new ContractBuilder.ProvidesBuilder().from(p);
			capabilities.add(processTemplate(element, pb.build()));

		}
	}

	private void processElement(Element element) {
//		System.out.println("  ===> Process base annotations ");

		processBaseAnnotatins(element);

//		System.out.println("  ===> Get all annotations ");

		for (AnnotationMirror annotationMirror : elementUtils.getAllAnnotationMirrors(element)) {
//			System.out.println("   ===> " + annotationMirror);
			String annotationName = annotationMirror.getAnnotationType().asElement().toString();
			String annotationPackage = annotationMirror.getAnnotationType().asElement().getEnclosingElement()
					.toString();

			if (!baseAnnotations.contains(annotationName) && !annotationPackage.startsWith("java.lang.annotation")) {
				processAdditionalAnnotation(annotationMirror);
			}
		}
	}

	private String processTemplate(Element element, String requirement) {
		if (element.getKind().equals(ElementKind.ANNOTATION_TYPE)) {

//			System.out.println("STACK : " + stack);
			try {
				Template t = new Template(element.getSimpleName().toString(), new StringReader(requirement),
						new Configuration());
				Map<String, Object> dataModel = new HashMap<>();
				stack.peek().entrySet() //
						.forEach(e -> dataModel.put(e.getKey().toString().replace("()", ""), e.getValue().getValue()));
				StringWriter out = new StringWriter();
				t.process(dataModel, out);
				return out.toString();
			} catch (IOException | TemplateException | SecurityException | IllegalArgumentException e) {
				messager.printMessage(Kind.ERROR, "IOException while reading temlate! " + e.getMessage());
				e.printStackTrace();
			}
		}
		return requirement;
	}

	private void processAdditionalAnnotation(AnnotationMirror annotation) {
//		System.out.println("   == additional => " + annotation);
//		System.out.println("   == values => " + elementUtils.getElementValuesWithDefaults(annotation));
		stack.push(elementUtils.getElementValuesWithDefaults(annotation));
		Element element = annotation.getAnnotationType().asElement();
		processElement(element);
		stack.pop();
	}

	public void generateFile() throws IOException {

		FileObject fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "em.generated.bnd");
		try (Writer writer = fileObject.openWriter()) {
			writer.write("Provide-Capability: " + String.join(",", capabilities));
			writer.write("\n");
			writer.write("Require-Capability: " + String.join(",", requrements));
		}
	}

}
