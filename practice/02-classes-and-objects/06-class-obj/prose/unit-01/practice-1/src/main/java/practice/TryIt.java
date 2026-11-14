package practice;

import java.util.List;
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
            Handler status = new StatusHandler();
            Handler acme = new InvoiceHandler("acme");
            Handler globex = new InvoiceHandler("globex");
            System.out.println("status routes: " + status.routes());
            System.out.println("acme routes: " + acme.routes());
            System.out.println("globex routes: " + globex.routes());
            CountingHandler counting = new CountingHandler();
            counting.routes();
            counting.routes();
            System.out.println("after three reloads: " + counting.routes());
            System.out.println("paths built: " + counting.builds);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final class StatusHandler extends Handler {
        @Override protected List<String> paths() { return List.of("/status", "/live"); }
    }

    static final class InvoiceHandler extends Handler {
        private final String tenant;

        InvoiceHandler(String tenant) { this.tenant = tenant; }

        @Override protected List<String> paths() {
            return List.of("/" + tenant + "/invoices", "/" + tenant + "/invoices/{id}");
        }
    }

    static final class CountingHandler extends Handler {
        int builds;

        @Override protected List<String> paths() {
            builds++;
            return List.of("/stats");
        }
    }
}
