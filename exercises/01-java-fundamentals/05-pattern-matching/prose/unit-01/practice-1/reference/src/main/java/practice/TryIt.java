package practice;

import java.util.ArrayList;
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
            List<String> audit = new ArrayList<>();
            EventBus bus = new EventBus();
            bus.subscribe(Auditable.class, e -> audit.add(e.getClass().getSimpleName()));
            bus.subscribe(RefundEvent.class, e -> { });
            System.out.println("RefundIssued delivered to: " + bus.publish(new RefundIssued()) + " handlers");
            System.out.println("RefundRejected delivered to: " + bus.publish(new RefundRejected()) + " handlers");
            System.out.println("unmapped (null) delivered to: " + bus.publish(null) + " handlers");
            System.out.println("audit log: " + audit);
            List<Class<?>> known = List.of(RefundIssued.class, RefundRejected.class, ReportBuilt.class);
            System.out.println("startup check, unhandled: " + bus.unhandled(known).stream().map(Class::getSimpleName).toList());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    interface Auditable { }
    static class RefundEvent { }
    static class RefundIssued extends RefundEvent { }
    static class RefundRejected extends RefundEvent implements Auditable { }
    static class ReportBuilt { }
}
