package practice;

import java.util.function.Consumer;
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
            DeployNotifier notifier = new DeployNotifier();
            Consumer<String> fullRollout = event -> System.out.println("full-rollout heard " + event);
            Consumer<String> reminder = new Consumer<>() {
                @Override public void accept(String event) {
                    System.out.println("reminder heard " + event);
                    notifier.unsubscribe(this);
                }
            };
            notifier.subscribe(reminder);
            notifier.subscribe(event -> {
                System.out.println("canary heard " + event);
                notifier.subscribe(fullRollout);
            });
            notifier.subscribe(event -> System.out.println("search heard " + event));
            System.out.println("release-3.4 delivered to: " + notifier.deploy("release-3.4"));
            System.out.println("release-3.5 delivered to: " + notifier.deploy("release-3.5"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
