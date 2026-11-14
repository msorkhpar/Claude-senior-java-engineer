package practice;

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
            SpanTable table = new SpanTable();
            SpanTable.Request checkout = new SpanTable.Request("q-501", "POST", "/checkout", 128);
            table.event(checkout, "cart priced");
            table.event(checkout, "payment taken");
            SpanTable.Span span = table.spanFor(checkout);
            System.out.println("label: " + span.label());
            System.out.println("events: " + span.events());
            handleAndForget(table);
            for (int i = 0; i < 20 && table.size() > 1; i++) System.gc();
            System.out.println("spans after the finished request is collected: " + table.size());
            System.out.println("checkout still traced: " + table.spanFor(checkout).events().size() + " events");
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static void handleAndForget(SpanTable table) {
        SpanTable.Request basket = new SpanTable.Request("q-502", "GET", "/basket/9", 512 * 1024);
        table.event(basket, "loaded");
        System.out.println("spans while both are held: " + table.size());
    }
}
