package practice;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            CountDownLatch supplierWakesUp = new CountDownLatch(1);
            ExecutorService pool = Executors.newSingleThreadExecutor();
            try {
                PriceService.Supplier supplier = sku -> switch (sku) {
                    case "sku-210" -> 1_899L;
                    case "sku-555" -> { supplierWakesUp.await(); yield 26_000L; }
                    default -> throw new ConnectException("prices.example.invalid: connection refused");
                };
                Map<String, Long> cached = Map.of("sku-210", 1_950L, "sku-555", 24_990L, "sku-777", 650L);
                PriceService prices = new PriceService(pool, supplier, cached, 150);
                System.out.println("sku-210 (supplier answers): " + prices.priceOf("sku-210"));
                System.out.println("sku-555 (supplier stuck): " + prices.priceOf("sku-555"));
                System.out.println("sku-777 (supplier down): " + prices.priceOf("sku-777"));
                System.out.println("sku-210 after the stuck call: " + prices.priceOf("sku-210"));
            } finally {
                supplierWakesUp.countDown();
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
