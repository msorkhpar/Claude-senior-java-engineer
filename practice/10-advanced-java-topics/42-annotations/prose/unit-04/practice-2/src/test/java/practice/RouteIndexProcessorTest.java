package practice;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/** Compiles small handler sources with javac and RouteIndexProcessor, as the server's build does. */
class RouteIndexProcessorTest {

    @TempDir
    Path out;

    /**
     * Stands in for another processor of the build (an OpenAPI client generator, say): on its
     * first call it writes one handler class, itself marked with @Route, and on its second call
     * a payload class without any annotation.
     */
    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_21)
    static class GeneratesAHandler extends AbstractProcessor {
        private final String className;
        private final String path;
        private int calls;

        GeneratesAHandler(String className, String path) {
            this.className = className;
            this.path = path;
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
            calls++;
            if (calls == 1) write(className, "@practice.RouteIndexProcessor.Route(\"" + path + "\")\n");
            if (calls == 2) write(className + "Payload", "");
            return false;
        }

        private void write(String name, String annotation) {
            try (Writer w = processingEnv.getFiler().createSourceFile("shop." + name).openWriter()) {
                w.write("package shop;\n\n" + annotation + "public class " + name + " { }\n");
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    record Result(boolean compiled, List<Diagnostic<? extends JavaFileObject>> diagnostics, Path generated) {
        List<String> errors() {
            return diagnostics.stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                    .map(d -> d.getMessage(Locale.ROOT)).toList();
        }

        String routes() throws IOException {
            return Files.readString(generated);
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

    static JavaFileObject handler(String className, String path) {
        return source(className, "package shop;\n\n" + "@practice.RouteIndexProcessor.Route(\"" + path + "\")\n"
                + "public class " + className + " { }\n");
    }

    /** A class that reads the generated table, so the build fails if the table isn't compiled. */
    static JavaFileObject server() {
        return source("Server", "package shop;\n\npublic class Server {\n"
                + "    static java.util.Map<String, String> routes() { return practice.generated.Routes.ALL; }\n}\n");
    }

    Result compile(List<Processor> others, JavaFileObject... sources) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        Path classes = Files.createDirectories(out.resolve("classes"));
        Path generated = Files.createDirectories(out.resolve("generated"));
        String processorClasses;
        try {
            processorClasses = Path.of(RouteIndexProcessor.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        List<String> options = List.of("--release", "21", "-proc:full",
                "-classpath", processorClasses, "-d", classes.toString(), "-s", generated.toString());
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javac.getTask(null, null, diagnostics, options, null, List.of(sources));
        List<Processor> processors = new ArrayList<>(others);
        processors.add(new RouteIndexProcessor());
        task.setProcessors(processors);
        task.setLocale(Locale.ROOT);
        boolean ok = task.call();
        return new Result(ok, new ArrayList<>(diagnostics.getDiagnostics()),
                generated.resolve("practice/generated/Routes.java"));
    }

    @Test
    void handlersInTheSourcesAreInTheTable() throws IOException {
        Result r = compile(List.of(),
                handler("InvoiceHandler", "/invoices"),
                handler("ParcelHandler", "/parcels"),
                server());

        assertThat(r.errors()).isEmpty();
        assertThat(r.compiled()).isTrue();
        assertThat(r.routes()).contains("\"/invoices\", \"shop.InvoiceHandler\"", "\"/parcels\", \"shop.ParcelHandler\"");
    }

    @Test
    void aGeneratedHandlerDoesNotBreakTheBuild() throws IOException {
        Result r = compile(List.of(new GeneratesAHandler("WebhookHandler", "/webhooks")),
                handler("InvoiceHandler", "/invoices"),
                server());

        assertThat(r.errors()).isEmpty();
        assertThat(r.compiled()).isTrue();
    }

    @Test
    void aGeneratedHandlerIsInTheTable() throws IOException {
        Result r = compile(List.of(new GeneratesAHandler("RefundCallbackHandler", "/refund-callbacks")),
                handler("ParcelHandler", "/parcels"),
                server());

        assertThat(r.compiled()).isTrue();
        assertThat(r.routes()).contains("\"/parcels\", \"shop.ParcelHandler\"",
                "\"/refund-callbacks\", \"shop.RefundCallbackHandler\"");
    }
}
