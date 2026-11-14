package practice;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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
            int[] computed = {0};
            Map<String, String> leases = Map.of("berth-1", "Example Shipping");
            Memo<String, String> owner = new Memo<>(berth -> {
                computed[0]++;
                return leases.get(berth);
            });
            System.out.println("owner of berth-1: " + owner.apply("berth-1"));
            System.out.println("owner of berth-7: " + owner.apply("berth-7"));
            System.out.println("owner of berth-7 again: " + owner.apply("berth-7"));
            System.out.println("owner lookups made: " + computed[0]);
            int[] costed = {0};
            AtomicReference<Function<Integer, Long>> self = new AtomicReference<>();
            Memo<Integer, Long> cost = new Memo<>(section -> {
                costed[0]++;
                return section == 0 ? 50L : 50L + section + self.get().apply(section - 1);
            });
            self.set(cost);
            System.out.println("cost of section 5: " + cost.apply(5));
            System.out.println("cost of section 3: " + cost.apply(3));
            System.out.println("sections worked out: " + costed[0]);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
