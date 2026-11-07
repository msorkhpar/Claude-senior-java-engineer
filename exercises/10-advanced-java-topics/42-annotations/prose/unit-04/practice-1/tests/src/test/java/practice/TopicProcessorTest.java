package practice;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/** Compiles small listener sources with javac and TopicProcessor, as the service's build does. */
class TopicProcessorTest {

    @TempDir
    Path out;

    record Result(boolean compiled, List<Diagnostic<? extends JavaFileObject>> diagnostics, Path generated) {
        List<Diagnostic<? extends JavaFileObject>> errors() {
            return diagnostics.stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).toList();
        }
    }

    static JavaFileObject source(String className, String code) {
        return new SimpleJavaFileObject(URI.create("string:///shop/" + className + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return code;
            }
        };
    }

    static JavaFileObject listener(String className, String topic) {
        return source(className, "package shop;\n"
                + "\n"
                + "import practice.TopicProcessor.Topic;\n"
                + "@Topic(\"" + topic + "\") public class " + className + " { }\n");
    }

    Result compile(JavaFileObject... sources) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        Path classes = Files.createDirectories(out.resolve("classes"));
        Path generated = Files.createDirectories(out.resolve("generated"));
        String processorClasses;
        try {
            processorClasses = Path.of(TopicProcessor.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        List<String> options = List.of("--release", "21", "-proc:full",
                "-classpath", processorClasses, "-d", classes.toString(), "-s", generated.toString());
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javac.getTask(null, null, diagnostics, options, null, List.of(sources));
        task.setProcessors(List.of(new TopicProcessor()));
        task.setLocale(Locale.ROOT);
        boolean ok = task.call();
        return new Result(ok, new ArrayList<>(diagnostics.getDiagnostics()),
                generated.resolve("practice/generated/Topics.java"));
    }

    @Test
    void wellFormedTopicsCompileAndAreListed() throws IOException {
        Result r = compile(
                listener("InvoicePaidListener", "billing.invoice.paid"),
                listener("ReservationListener", "stock.reserved"),
                source("Startup", "package shop;\n"
                        + "public class Startup { static java.util.List<String> topics() { return practice.generated.Topics.ALL; } }\n"));

        assertThat(r.errors()).isEmpty();
        assertThat(r.compiled()).isTrue();
        assertThat(Files.readString(r.generated())).contains("\"billing.invoice.paid\"", "\"stock.reserved\"");
    }

    @Test
    void aMalformedTopicStopsTheBuild() throws IOException {
        Result r = compile(
                listener("InvoicePaidListener", "billing.invoice.paid"),
                listener("RefundListener", "Billing.Refunded"));

        assertThat(r.compiled()).as("javac's result").isFalse();
        assertThat(r.errors()).isNotEmpty();
    }

    @Test
    void theErrorIsReportedOnTheListenerThatHasTheTypo() throws IOException {
        Result r = compile(
                listener("InvoicePaidListener", "billing.invoice.paid"),
                listener("RefundListener", "billing..refunded"),
                listener("ReservationListener", "stock.reserved"));

        assertThat(r.errors()).isNotEmpty();
        assertThat(r.errors()).allSatisfy(d -> {
            assertThat(d.getSource()).as("the file the error is reported in").isNotNull();
            assertThat(d.getSource().getName()).endsWith("RefundListener.java");
            assertThat(d.getLineNumber()).as("the line the error is reported on").isEqualTo(4L);
        });
    }

    @Test
    void aMalformedTopicIsADiagnosticNotACrash() throws IOException {
        Result[] r = new Result[1];
        assertThatCode(() -> r[0] = compile(listener("ShipmentListener", "Shipping.Dispatched")))
                .as("javac must finish the compile and report, not stop with an exception")
                .doesNotThrowAnyException();
        assertThat(r[0].errors()).anySatisfy(d ->
                assertThat(d.getMessage(Locale.ROOT)).contains("Shipping.Dispatched"));
    }
}
