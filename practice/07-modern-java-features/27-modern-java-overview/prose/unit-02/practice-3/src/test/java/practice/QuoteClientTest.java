package practice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Runs the client against a quote service on 127.0.0.1 (port chosen by the system). Nothing
 * leaves the machine. Every call into the client goes through a Future with a time limit.
 */
class QuoteClientTest {

    private static final ThreadFactory DAEMONS = r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    };

    private final CountDownLatch release = new CountDownLatch(1);
    private final List<String> seen = new CopyOnWriteArrayList<>();
    private ExecutorService serverThreads;
    private ExecutorService callers;
    private HttpServer server;
    private HttpClient http;
    private URI base;

    @BeforeEach
    void startPartner() throws IOException {
        serverThreads = Executors.newCachedThreadPool(DAEMONS);
        callers = Executors.newCachedThreadPool(DAEMONS);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(serverThreads);
        server.createContext("/quotes/", this::answer);
        server.start();
        base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
        http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).executor(callers).build();
    }

    @AfterEach
    void stopPartner() {
        try {
            release.countDown();
            http.shutdownNow();
        } finally {
            server.stop(0);
            serverThreads.shutdownNow();
            callers.shutdownNow();
        }
    }

    private void answer(HttpExchange exchange) throws IOException {
        String symbol = exchange.getRequestURI().getPath().substring("/quotes/".length());
        seen.add(exchange.getRequestMethod() + " " + symbol);
        switch (symbol) {
            case "NRDC" -> reply(exchange, 200, "318.40");
            case "KLMT" -> reply(exchange, 200, "7.05\n");
            case "HOLD" -> {
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                reply(exchange, 200, "55.00");
            }
            case "BUSY" -> reply(exchange, 503, "{\"error\":\"maintenance window\"}");
            default -> reply(exchange, 404, "unknown symbol " + symbol);
        }
    }

    private static void reply(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /** Calls price() on another thread and waits at most five seconds for it to end. */
    private Future<BigDecimal> lookUp(QuoteClient client, String symbol) {
        return callers.submit(() -> client.price(symbol));
    }

    private QuoteClient client(Duration budget) {
        return new QuoteClient(http, base, budget);
    }

    private static Throwable failureOf(Future<BigDecimal> lookup) throws InterruptedException {
        try {
            BigDecimal price = lookup.get(5, TimeUnit.SECONDS);
            return fail("expected no price, got " + price);
        } catch (ExecutionException e) {
            return e.getCause();
        } catch (TimeoutException e) {
            return fail("the lookup was still running after 5 s");
        }
    }

    @Test
    void returnsThePriceThePartnerQuotes() throws Exception {
        QuoteClient client = client(Duration.ofSeconds(3));

        assertThat(lookUp(client, "NRDC").get(5, TimeUnit.SECONDS)).isEqualByComparingTo("318.40");
        assertThat(lookUp(client, "KLMT").get(5, TimeUnit.SECONDS)).isEqualByComparingTo("7.05");
        assertThat(seen).containsExactly("GET NRDC", "GET KLMT");
    }

    @Test
    void aPartnerThatStopsAnsweringEndsTheLookupWithinItsBudget() throws Exception {
        QuoteClient client = client(Duration.ofMillis(300));

        Throwable failure = failureOf(lookUp(client, "HOLD"));

        assertThat(failure).isInstanceOf(IOException.class);
    }

    @Test
    void aServerErrorIsNoPrice() throws Exception {
        QuoteClient client = client(Duration.ofSeconds(3));

        Throwable failure = failureOf(lookUp(client, "BUSY"));

        assertThat(failure).isInstanceOf(IOException.class);
    }

    @Test
    void anUnknownSymbolIsNoPrice() throws Exception {
        QuoteClient client = client(Duration.ofSeconds(3));

        Throwable failure = failureOf(lookUp(client, "ZZZQ"));

        assertThat(failure).isInstanceOf(IOException.class);
    }
}
