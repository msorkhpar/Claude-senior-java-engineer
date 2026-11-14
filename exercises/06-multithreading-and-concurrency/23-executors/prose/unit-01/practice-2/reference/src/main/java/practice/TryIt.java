package practice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            List<Throwable> alerts = Collections.synchronizedList(new ArrayList<>());
            List<Throwable> workerStops = Collections.synchronizedList(new ArrayList<>());
            SettlementPool pool = new SettlementPool(1, alerts::add);
            try {
                startWorkerIn(pool, workerStops);
                pool.execute(() -> System.out.println("merchant 18 paid"));
                pool.submit(() -> { throw new IOException("bank-3 timed out"); });
                pool.submit(() -> "merchant 19 paid");
                pool.submit(() -> { throw new IllegalArgumentException("IBAN rejected"); });
                pool.shutdown();
                System.out.println("pool finished: " + pool.awaitTermination(3, TimeUnit.SECONDS));
                if (!workerStops.isEmpty() && workerStops.get(0) instanceof RuntimeException stop) throw stop;
                System.out.println("jobs run: " + pool.getCompletedTaskCount());
                System.out.println("alerts raised: " + alerts.size());
                for (Throwable alert : alerts) System.out.println("alert: " + alert);
            } finally {
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Starts the pool's worker in a thread group that collects what escapes it, so Run can show it. */
    static void startWorkerIn(SettlementPool pool, List<Throwable> stops) throws InterruptedException {
        ThreadGroup group = new ThreadGroup("settlement") {
            @Override public void uncaughtException(Thread t, Throwable e) { stops.add(e); }
        };
        Thread starter = new Thread(group, pool::prestartAllCoreThreads);
        starter.start();
        starter.join();
    }
}
