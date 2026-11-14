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
            List<String> lookups = new ArrayList<>();
            OperationCache cache = new OperationCache((type, name, params) -> {
                lookups.add(type.getSimpleName() + "." + name);
                return type.getDeclaredMethod(name, params);
            });
            Queue orders = new Queue();
            System.out.println("grow: " + cache.run(orders, "grow", new Class<?>[] {int.class}, 16));
            System.out.println("grow again: " + cache.run(orders, "grow", new Class<?>[] {int.class}, 32));
            System.out.println("grow range: " + cache.run(orders, "grow", new Class<?>[] {int.class, int.class}, 8, 64));
            System.out.println("flush (private): " + cache.run(orders, "flush", new Class<?>[0]));
            System.out.println("lookups made: " + lookups);
            System.out.println("cached operations: " + cache.size());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static class Queue {
        int capacity = 10;
        int grow(int to) { capacity = to; return capacity; }
        String grow(int min, int max) { capacity = max; return min + "-" + max; }
        private String flush() { return "flushed at capacity " + capacity; }
    }
}
