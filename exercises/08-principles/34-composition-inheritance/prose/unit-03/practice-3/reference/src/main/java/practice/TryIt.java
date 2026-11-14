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
            Stocks.Shared shared = Stocks.shared(new Stocks.MapStock());
            RuntimeException[] failed = {null};
            List<Thread> checkouts = new ArrayList<>();
            for (int t = 0; t < 3; t++) {
                int lane = t;
                Thread checkout = new Thread(() -> {
                    try {
                        for (int i = 0; i < 1_000; i++) shared.stock().set("lane-" + lane + "-item-" + i, i % 7);
                    } catch (RuntimeException e) {
                        failed[0] = e;
                    }
                });
                checkouts.add(checkout);
                checkout.start();
            }
            for (Thread checkout : checkouts) checkout.join();
            if (failed[0] != null) throw failed[0];
            System.out.println("lane-2-item-999: " + shared.stock().level("lane-2-item-999"));
            try {
                shared.stock().set("lane-0-item-1", -5);
            } catch (IllegalArgumentException refused) {
                System.out.println("refused: " + refused.getMessage());
            }
            shared.stock().set("lane-0-item-1", 3);
            System.out.println("lane-0-item-1 after the refusal: " + shared.stock().level("lane-0-item-1"));
            System.out.println("succeeded: " + shared.metrics().succeeded() + ", failed: " + shared.metrics().failed());
            System.out.println("in flight now: " + shared.metrics().inFlight());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
