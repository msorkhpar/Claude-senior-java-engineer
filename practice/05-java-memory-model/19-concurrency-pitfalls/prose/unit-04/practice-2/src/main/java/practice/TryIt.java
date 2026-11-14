package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
            StartupGate gate = new StartupGate(3);
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Map<String, Runnable> loaders = Map.of(
                    "currencies", () -> { },
                    "stores", () -> { },
                    "discounts", () -> { throw new IllegalStateException("discount service down"); });
            List<Thread> threads = new ArrayList<>();
            loaders.forEach((cache, loader) -> {
                Thread t = new Thread(() -> gate.runLoader(cache, loader), cache);
                t.setUncaughtExceptionHandler((thread, e) -> failure.set(e));
                threads.add(t);
            });
            threads.forEach(Thread::start);
            for (Thread t : threads) t.join(5_000);
            if (failure.get() != null) throw new IllegalStateException("a loader thread failed: " + failure.get());
            System.out.println("ready after all loaders finished: " + gate.awaitLoaded(2, TimeUnit.SECONDS));
            System.out.println("caches missing: " + gate.failed());
            StartupGate oneShort = new StartupGate(2);
            oneShort.runLoader("currencies", () -> { });
            System.out.println("ready with one loader never run (200 ms): " + oneShort.awaitLoaded(200, TimeUnit.MILLISECONDS));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
