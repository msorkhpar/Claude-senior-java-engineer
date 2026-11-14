package practice;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
            for (Logger jdkLogger : JDK_NOISE) jdkLogger.setLevel(Level.INFO);
            CountDownLatch partnerWakesUp = new CountDownLatch(1);
            ExecutorService serverThreads = Executors.newCachedThreadPool();
            HttpServer partner = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            partner.setExecutor(serverThreads);
            partner.createContext("/quotes/", exchange -> {
                String symbol = exchange.getRequestURI().getPath().substring("/quotes/".length());
                switch (symbol) {
                    case "ORBX" -> reply(exchange, 200, "142.75");
                    case "SLOW" -> {
                        try {
                            partnerWakesUp.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        reply(exchange, 200, "1.00");
                    }
                    case "MAINT" -> reply(exchange, 503, "{\"error\":\"maintenance\"}");
                    default -> reply(exchange, 404, "unknown symbol " + symbol);
                }
            });
            partner.start();
            HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            try {
                URI base = URI.create("http://127.0.0.1:" + partner.getAddress().getPort());
                QuoteClient quotes = new QuoteClient(http, base, Duration.ofMillis(400));
                System.out.println("ORBX: " + quotes.price("ORBX"));
                for (String symbol : new String[] {"SLOW", "MAINT", "NOPE"}) {
                    try {
                        System.out.println(symbol + ": " + quotes.price(symbol));
                    } catch (IOException noPrice) {
                        System.out.println(symbol + ": no price, " + noPrice.getClass().getSimpleName());
                    }
                }
            } finally {
                partnerWakesUp.countDown();
                http.shutdownNow();
                partner.stop(0);
                serverThreads.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** The JDK's own HTTP and security loggers, held at INFO so their start-up detail stays out of the output. */
    private static final Logger[] JDK_NOISE = {Logger.getLogger("com.sun.net.httpserver"),
            Logger.getLogger("javax.net.ssl"), Logger.getLogger("jdk.internal.httpclient.debug"),
            Logger.getLogger("jdk.httpclient.HttpClient"), Logger.getLogger("jdk.event.security")};

    static void reply(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
