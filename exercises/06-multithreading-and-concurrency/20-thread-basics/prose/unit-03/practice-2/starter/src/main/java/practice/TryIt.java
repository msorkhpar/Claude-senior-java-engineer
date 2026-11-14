package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
            BlockingQueue<String> outbox = new LinkedBlockingQueue<>(List.of("order.shipped#41", "order.shipped#42"));
            List<String> posts = Collections.synchronizedList(new ArrayList<>());
            List<Long> pauses = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch bothDelivered = new CountDownLatch(2);
            WebhookSender.Endpoint endpoint = payload -> {
                posts.add(payload);
                boolean accepted = !(payload.endsWith("#41") && posts.size() < 3);
                if (accepted) bothDelivered.countDown();
                return accepted;
            };
            AtomicReference<Throwable> failure = new AtomicReference<>();
            ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "webhooks");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler((thread, e) -> failure.set(e));
                return t;
            });
            pool.execute(new WebhookSender(outbox, endpoint, millis -> {
                pauses.add(millis);
                if (Thread.interrupted()) throw new InterruptedException();
            }, 4));
            for (int i = 0; i < 50 && failure.get() == null && !bothDelivered.await(100, TimeUnit.MILLISECONDS); i++) { }
            if (failure.get() instanceof RuntimeException e) throw e;
            System.out.println("both delivered: " + (bothDelivered.getCount() == 0));
            pool.shutdownNow();
            System.out.println("sender ended on shutdownNow: " + pool.awaitTermination(5, TimeUnit.SECONDS));
            System.out.println("requests sent: " + posts);
            System.out.println("pauses taken (ms): " + pauses);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
