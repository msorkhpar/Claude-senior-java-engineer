package practice;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("practice.FieldNamesProcessor.FieldNames")
public final class FieldNamesProcessor extends AbstractProcessor {

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public @interface FieldNames {
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        for (Element element : round.getElementsAnnotatedWith(FieldNames.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR, "@FieldNames needs a class", element);
                continue;
            }
            generate((TypeElement) element);
        }
        return true;
    }

    private void generate(TypeElement type) {
        String pkg = processingEnv.getElementUtils().getPackageOf(type).getQualifiedName().toString();
        String name = type.getSimpleName() + "Fields";
        List<String> quoted = new ArrayList<>();
        for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (!field.getModifiers().contains(Modifier.STATIC)) {
                quoted.add("\"" + field.getSimpleName() + "\"");
            }
        }
        String qualified = pkg.isEmpty() ? name : pkg + "." + name;
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(qualified, type);
            try (Writer out = file.openWriter()) {
                if (!pkg.isEmpty()) {
                    out.write("package " + pkg + ";\n");
                }
                out.write("public final class " + name + " {\n"
                        + "    private " + name + "() {}\n"
                        + "    public static final java.util.List<String> NAMES = java.util.List.of("
                        + String.join(", ", quoted) + ");\n"
                        + "}\n");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Failed to generate " + qualified + ": " + e.getMessage(), type);
        }
    }
}
