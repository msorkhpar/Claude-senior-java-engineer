package practice;

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
            LabelPrinter.Cities directory = postcode -> switch (postcode) {
                case "75001" -> "Paris";
                case "28013" -> "Madrid";
                case "00184" -> "Rome";
                default -> "Unknown";
            };
            LabelPrinter east = new LabelPrinter(directory);
            LabelPrinter west = new LabelPrinter(directory);
            long before = LabelPrinter.renderedByAll();
            String[] labels = new String[2];
            Thread first = new Thread(() -> labels[0] = east.render("Sam Placeholder", "1 Rue Example", "75001"));
            Thread second = new Thread(() -> labels[1] = east.render("Ana Sample", "Calle Mayor 2", "28013"));
            first.start();
            second.start();
            first.join();
            second.join();
            String third = west.render("Pat Example", "Via Nazionale 5", "00184");
            System.out.println("label 1: " + labels[0].replace("\n", " / "));
            System.out.println("label 2: " + labels[1].replace("\n", " / "));
            System.out.println("label 3: " + third.replace("\n", " / "));
            System.out.println("rendered by all printers: " + (LabelPrinter.renderedByAll() - before));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
