package practice;

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
            List<Reconciler.Invoice> open = List.of(
                    new Reconciler.Invoice("INV-3301", "RF44 1000 2000 3001"),
                    new Reconciler.Invoice("INV-3302", null),
                    new Reconciler.Invoice("INV-3303", "RF60 9000 8000 7003"));
            String file = "PAY-1;RF60 9000 8000 7003\nPAY-2; \nPAY-3;RF44 1000 2000 3001\nPAY-4;RF12 0000 0000 0004\n";
            List<Reconciler.Payment> payments = Reconciler.readStatement(file);
            System.out.println("payments read: " + payments.size());
            Map<String, String> matched = new Reconciler().match(open, payments);
            System.out.println("matched: " + matched);
            System.out.println("manual review: " + (payments.size() - matched.size()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
