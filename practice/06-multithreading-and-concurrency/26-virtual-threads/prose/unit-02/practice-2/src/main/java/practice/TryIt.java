package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            WorkerGauge gauge = new WorkerGauge("checkout-");
            CountDownLatch paymentAnswers = new CountDownLatch(1);
            CountDownLatch started = new CountDownLatch(3);
            String[] name = new String[1];
            try (ExecutorService executor = Executors.newThreadPerTaskExecutor(gauge)) {
                try {
                    for (int i = 0; i < 3; i++) {
                        executor.submit(() -> {
                            name[0] = Thread.currentThread().getName() + ", virtual " + Thread.currentThread().isVirtual();
                            started.countDown();
                            return paymentAnswers.await(5, TimeUnit.SECONDS);
                        });
                    }
                    started.await(5, TimeUnit.SECONDS);
                    System.out.println("a request ran on: " + name[0].replaceAll("\\d+,", "N,"));
                    System.out.println("busy while 3 requests wait for payment: " + gauge.busy());
                } finally {
                    paymentAnswers.countDown();
                }
            }
            System.out.println("busy once they are done: " + settled(gauge));
            Thread declined = gauge.newThread(() -> { throw new IllegalStateException("card declined"); });
            System.out.println("busy with a thread made but not started: " + gauge.busy());
            declined.setUncaughtExceptionHandler((thread, e) -> System.out.println("request failed: " + e.getMessage()));
            declined.start();
            declined.join();
            System.out.println("busy after the failed request: " + settled(gauge));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** The gauge once it reads 0, or after a second: the last request's thread may still be ending. */
    static int settled(WorkerGauge gauge) throws InterruptedException {
        long deadline = System.nanoTime() + 1_000_000_000L;
        while (gauge.busy() != 0 && System.nanoTime() < deadline) Thread.sleep(5);
        return gauge.busy();
    }
}
