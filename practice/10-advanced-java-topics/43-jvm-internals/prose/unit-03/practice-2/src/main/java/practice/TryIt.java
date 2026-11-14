package practice;

import java.util.concurrent.atomic.AtomicLong;
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
            AtomicLong fakeNow = new AtomicLong(0);
            MicroBench bench = new MicroBench(fakeNow::get);
            MicroBench.Report report = bench.measure(x -> {
                fakeNow.addAndGet(40);
                return x * x;
            }, new long[] {2, 3, 10}, 2);
            System.out.println("fake clock, ns per call: " + report.nanosPerCall());
            System.out.println("fake clock, checksum: " + report.checksum());
            long[] coupons = new long[10_000];
            for (int i = 0; i < coupons.length; i++) coupons[i] = 1_000 + i;
            MicroBench.Report real = MicroBench.withSystemClock().measure(c -> Long.hashCode(c * 31 + 7) % 97, coupons, 5);
            System.out.println("real clock, ns per call above zero: " + (real.nanosPerCall() > 0));
            System.out.println("real clock, checksum: " + real.checksum());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
