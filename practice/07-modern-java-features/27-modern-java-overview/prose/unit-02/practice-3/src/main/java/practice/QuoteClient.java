package practice;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Looks up prices at a partner's quote service: GET {base}/quotes/{symbol} answers 200 with the
 * price as a plain decimal. The checkout calls it on its request threads, so a lookup must end
 * within its time budget, with a price or with an IOException.
 */
public class QuoteClient {

    private final HttpClient http;
    private final URI base;
    private final Duration budget;

    /**
     * @param http   the service's one shared client
     * @param base   the partner's base address, such as http://127.0.0.1:8080
     * @param budget how long one lookup may take in total
     */
    public QuoteClient(HttpClient http, URI base, Duration budget) {
        this.http = http;
        this.base = base;
        this.budget = budget;
    }

    /** The current price of {@code symbol}; IOException when the partner gave no price. */
    public BigDecimal price(String symbol) throws IOException, InterruptedException {
        HttpResponse<String> response = http.send(request(symbol), HttpResponse.BodyHandlers.ofString());
        return priceIn(response);
    }

    /** The request for one symbol. */
    HttpRequest request(String symbol) {
        // TODO: A GET of {base}/quotes/{symbol}. The partner sometimes accepts the connection and
        //       then doesn't answer for minutes; the lookup must give up within its budget.
        throw new IllegalStateException("TODO: request");
    }

    /** The price a response carries. */
    BigDecimal priceIn(HttpResponse<String> response) throws IOException {
        // TODO: Read the price from the body. Only a 200 carries a price; any other answer means
        //       there is no price, and the checkout must get an IOException, never a number.
        throw new IllegalStateException("TODO: priceIn");
    }
}
