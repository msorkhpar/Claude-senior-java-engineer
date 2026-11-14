package practice;

import java.util.ArrayList;
import java.util.List;
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
            AlertRegistry registry = new AlertRegistry();
            List<String> log = new ArrayList<>();
            Consumer<AlertRegistry.Tick> above = tick -> log.add("above-200: " + tick.symbol() + " " + tick.priceCents());
            Consumer<AlertRegistry.Tick> below = tick -> log.add("below-150: " + tick.symbol() + " " + tick.priceCents());
            Consumer<AlertRegistry.Tick> adder = tick -> registry.subscribe(t -> log.add("added mid-publish: " + t.symbol()));
            registry.subscribe(above);
            registry.subscribe(below);
            registry.subscribe(adder);
            System.out.println("first tick reached: " + registry.publish(new AlertRegistry.Tick("ORBX", 20_410)));
            registry.unsubscribe(below);
            registry.unsubscribe(adder);
            System.out.println("second tick reached: " + registry.publish(new AlertRegistry.Tick("ORBX", 20_575)));
            for (String line : log) System.out.println(line);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
