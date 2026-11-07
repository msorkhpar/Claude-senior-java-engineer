package practice;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Compile-time support for message listeners. Every class marked {@code @Topic("orders.created")}
 * is a listener for that broker topic, and the build writes {@code practice.generated.Topics},
 * whose {@code ALL} list the service subscribes to at startup.
 */
@SupportedAnnotationTypes("practice.TopicProcessor.Topic")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class TopicProcessor extends AbstractProcessor {

    /** Marks a listener class with the broker topic it listens to. */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public @interface Topic {
        String value();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        if (annotations.isEmpty()) return false;
        Set<String> topics = new TreeSet<>();
        for (Element listener : round.getElementsAnnotatedWith(Topic.class)) {
            String topic = listener.getAnnotation(Topic.class).value();
            check(listener, topic);
            topics.add(topic);
        }
        writeTopics(topics);
        return true;
    }

    /** The broker accepts lower-case words separated by single dots: "orders.created". */
    static boolean wellFormed(String topic) {
        return topic.matches("[a-z]+(\\.[a-z]+)*");
    }

    /** Called for every listener class with the topic its annotation names. */
    void check(Element listener, String topic) {
        if (!wellFormed(topic)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "topic \"" + topic + "\" is not lower-case words separated by dots", listener);
        }
    }

    private void writeTopics(Set<String> topics) {
        StringBuilder list = new StringBuilder();
        for (String t : topics) {
            if (list.length() > 0) list.append(", ");
            list.append('"').append(t).append('"');
        }
        try (Writer w = processingEnv.getFiler().createSourceFile("practice.generated.Topics").openWriter()) {
            w.write("package practice.generated;\n\n"
                    + "public final class Topics {\n"
                    + "    public static final java.util.List<String> ALL = java.util.List.of(" + list + ");\n"
                    + "}\n");
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
    }
}
