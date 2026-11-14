package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) {
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
            ClassLoader loader = TryIt.class.getClassLoader();
            Map<String, String> config = new LinkedHashMap<>();
            config.put("csv", "practice.TryIt$CsvExporter");
            config.put("audit", "practice.TryIt$AuditExporter");
            ExporterRegistry registry = new ExporterRegistry(config, loader);
            System.out.println("setup run while building: " + SETUP);
            System.out.println("csv: " + registry.create("csv").export("June sales"));
            System.out.println("audit: " + registry.create("audit").export("June sales"));
            System.out.println("setup run after first use: " + SETUP);
            try {
                new ExporterRegistry(Map.of("pdf", "practice.TryIt$PdfExportr"), loader);
            } catch (ExporterRegistry.ConfigException bad) {
                System.out.println("misspelt entry: " + bad.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final List<String> SETUP = new ArrayList<>();
    public static class CsvExporter implements ExporterRegistry.Exporter {
        public String export(String report) { return report.replace(' ', ';'); }
    }
    public static class AuditExporter implements ExporterRegistry.Exporter {
        static { SETUP.add("audit template loaded"); }
        public String export(String report) { return "[audited] " + report; }
    }
}
