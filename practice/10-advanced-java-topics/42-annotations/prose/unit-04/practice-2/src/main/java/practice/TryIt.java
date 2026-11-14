package practice;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            Path out = Files.createTempDirectory("tryit-routes");
            try {
                JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
                String classpath = Path.of(RouteIndexProcessor.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
                Path classes = Files.createDirectories(out.resolve("classes"));
                Path generated = Files.createDirectories(out.resolve("generated"));
                List<String> options = List.of("--release", "21", "-proc:full", "-classpath", classpath,
                        "-d", classes.toString(), "-s", generated.toString());
                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                JavaCompiler.CompilationTask task = javac.getTask(null, null, diagnostics, options, null, List.of(
                        source("CartHandler", "@practice.RouteIndexProcessor.Route(\"/cart\")\npublic class CartHandler { }\n"),
                        source("Server", "public class Server { static Object routes() { return practice.generated.Routes.ALL; } }\n")));
                task.setProcessors(List.of(new ClientGenerator(), new RouteIndexProcessor()));
                task.setLocale(Locale.ROOT);
                boolean ok = task.call();
                System.out.println("compiled: " + ok);
                for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                    if (d.getKind() == Diagnostic.Kind.ERROR) System.out.println("error: " + d.getMessage(Locale.ROOT));
                }
                Path routes = generated.resolve("practice/generated/Routes.java");
                if (Files.exists(routes)) {
                    Files.readAllLines(routes).stream().filter(l -> l.contains("ALL")).forEach(l -> System.out.println("table: " + l.strip()));
                } else {
                    System.out.println("table: not written");
                }
            } finally {
                try (var files = Files.walk(out)) {
                    files.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static JavaFileObject source(String name, String body) {
        String code = "package shop;\n\n" + body;
        return new SimpleJavaFileObject(URI.create("string:///shop/" + name + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return code; }
        };
    }

    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_21)
    static class ClientGenerator extends AbstractProcessor {
        private int calls;
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
            calls++;
            if (calls == 1) write("PaymentWebhookHandler", "@practice.RouteIndexProcessor.Route(\"/hooks/payment\")\n");
            if (calls == 2) write("PaymentWebhookHandlerDto", "");
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
}
