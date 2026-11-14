package practice;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
            List<String> serviceCalls = new CopyOnWriteArrayList<>();
            RateCache cache = new RateCache(zone -> {
                serviceCalls.add(zone);
                return zone.equals("ZONE-ADRIATIC") ? 1_640 : 2_000;
            });
            cache.replace("ZONE-BENELUX", 990);
            System.out.println("ZONE-BENELUX: " + cache.rateFor("ZONE-BENELUX"));
            System.out.println("ZONE-ADRIATIC (not cached yet): " + cache.rateFor("ZONE-ADRIATIC"));
            System.out.println("ZONE-ADRIATIC again: " + cache.rateFor("ZONE-ADRIATIC"));
            cache.rw.readLock().lock();
            try {
                CompletableFuture<Integer> sideBySide = CompletableFuture.supplyAsync(() -> cache.rateFor("ZONE-BENELUX"));
                System.out.println("read while another checkout reads: " + sideBySide.get(2, TimeUnit.SECONDS));
            } finally {
                cache.rw.readLock().unlock();
            }
            System.out.println("rates service calls: " + serviceCalls);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
