package practice;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Builds the HTTP route table at compile time. Every class marked {@code @Route("/orders")}
 * handles that path, and the build writes {@code practice.generated.Routes}, whose {@code ALL}
 * map (path to handler class name) the server reads at startup. Other processors in the same
 * build generate handler classes of their own, marked {@code @Route} too.
 */
@SupportedAnnotationTypes("practice.RouteIndexProcessor.Route")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class RouteIndexProcessor extends AbstractProcessor {

    /** Marks an HTTP handler class with the path it serves. */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public @interface Route {
        String value();
    }

    /** Path to the qualified name of the class that handles it. */
    private final Map<String, String> routes = new TreeMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        record(round);
        if (timeToWrite(annotations, round)) {
            writeIndex();
        }
        return true;
    }

    /** Called on every call of process: note the routes this call can see. */
    void record(RoundEnvironment round) {
        for (Element handler : round.getElementsAnnotatedWith(Route.class)) {
            routes.put(handler.getAnnotation(Route.class).value(),
                    ((TypeElement) handler).getQualifiedName().toString());
        }
    }

    /** Called on every call of process, after record: whether to write the route table now. */
    boolean timeToWrite(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        return !annotations.isEmpty();
    }

    private void writeIndex() {
        StringBuilder entries = new StringBuilder();
        routes.forEach((path, handler) -> {
            if (entries.length() > 0) entries.append(", ");
            entries.append('"').append(path).append("\", \"").append(handler).append('"');
        });
        try (Writer w = processingEnv.getFiler().createSourceFile("practice.generated.Routes").openWriter()) {
            w.write("package practice.generated;\n\n"
                    + "public final class Routes {\n"
                    + "    public static final java.util.Map<String, String> ALL = java.util.Map.of(" + entries + ");\n"
                    + "}\n");
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
    }
}
