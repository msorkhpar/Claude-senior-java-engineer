package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
            CountDownLatch burstEnds = new CountDownLatch(1);
            AtomicInteger readByOcr = new AtomicInteger();
            StringBuilder lastReader = new StringBuilder();
            ThreadPoolExecutor pool = OcrPool.create();
            try {
                for (int scan = 1; scan <= 11; scan++) {
                    pool.execute(() -> {
                        if (Thread.currentThread().getName().startsWith("ocr-")) readByOcr.incrementAndGet();
                        try {
                            burstEnds.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
                System.out.println("workers during the burst: " + pool.getPoolSize());
                System.out.println("scans waiting: " + pool.getQueue().size());
                pool.execute(() -> lastReader.append(Thread.currentThread().getName()));
                System.out.println("scan 12 read by the uploader itself: "
                        + lastReader.toString().equals(Thread.currentThread().getName()));
                burstEnds.countDown();
                pool.shutdown();
                System.out.println("pool finished: " + pool.awaitTermination(3, TimeUnit.SECONDS));
                System.out.println("scans read on ocr threads: " + readByOcr.get());
            } finally {
                burstEnds.countDown();
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
