package practice;

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
            Path out = Files.createTempDirectory("tryit-topics");
            try {
                List<String> errors = new ArrayList<>();
                boolean compiled = compile(out, errors,
                        listener("StockLowListener", "warehouse.stock.low"),
                        listener("CouponListener", "Marketing.CouponUsed"));
                System.out.println("compiled with a bad topic: " + compiled);
                for (String error : errors) System.out.println("error: " + error);
                Path good = Files.createTempDirectory(out, "good");
                boolean clean = compile(good, new ArrayList<>(),
                        listener("StockLowListener", "warehouse.stock.low"),
                        listener("CouponListener", "marketing.coupon.used"));
                System.out.println("compiled with good topics: " + clean);
                System.out.println("wellFormed(\"a..b\"): " + TopicProcessor.wellFormed("a..b"));
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

    private static JavaFileObject listener(String name, String topic) {
        String code = "package shop;\n\nimport practice.TopicProcessor.Topic;\n"
                + "@Topic(\"" + topic + "\") public class " + name + " { }\n";
        return new SimpleJavaFileObject(URI.create("string:///shop/" + name + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return code; }
        };
    }

    private static boolean compile(Path out, List<String> errors, JavaFileObject... sources) throws Exception {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        String classpath = Path.of(TopicProcessor.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Path classes = Files.createDirectories(out.resolve("classes"));
        Path generated = Files.createDirectories(out.resolve("generated"));
        List<String> options = List.of("--release", "21", "-proc:full", "-classpath", classpath,
                "-d", classes.toString(), "-s", generated.toString());
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javac.getTask(null, null, diagnostics, options, null, List.of(sources));
        task.setProcessors(List.of(new TopicProcessor()));
        task.setLocale(Locale.ROOT);
        boolean ok = task.call();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            if (d.getKind() == Diagnostic.Kind.ERROR) {
                String where = d.getSource() == null ? "?" : d.getSource().getName() + ":" + d.getLineNumber();
                errors.add(where + " " + d.getMessage(Locale.ROOT));
            }
        }
        return ok;
    }
}
