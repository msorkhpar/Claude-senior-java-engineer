package practice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
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
            List<String> actions = new ArrayList<>();
            Interceptors registry = new Interceptors();
            registry.register(Logged.class, m -> actions.add("log before " + m));
            System.out.println("charge returns: " + registry.call(new Payments(), "charge"));
            System.out.println("status returns: " + registry.call(new Payments(), "status"));
            System.out.println("actions run: " + actions);
            System.out.println("policy of @Sketch: " + Interceptors.policyOf(Sketch.class));
            try {
                registry.register(Sketch.class, actions::add);
            } catch (IllegalArgumentException refused) {
                System.out.println("refused: " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    @Retention(RetentionPolicy.RUNTIME) @interface Logged { }
    @interface Sketch { }
    static class Payments {
        @Logged String charge() { return "charged"; }
        String status() { return "ok"; }
    }
}
