package practice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/** Asks every supplier for a price at the same time and picks the cheapest offer. */
public class QuoteBoard {

    /** An offer: a supplier's price in cents. */
    public record Quote(String supplier, long cents) {}

    /** A check that couldn't be completed; the cause is what the supplier threw. */
    public static class QuoteFailed extends RuntimeException {
        public QuoteFailed(String supplier, Throwable cause) {
            super(supplier + " gave no answer", cause);
        }
    }

    /**
     * Asks every supplier, each on a thread of its own, and returns the cheapest offer. A supplier
     * whose call returns null doesn't stock the item and is left out. A supplier that fails fails
     * the whole check with QuoteFailed, since a price check that silently ignores a supplier
     * isn't a price check.
     */
    public static Optional<Quote> cheapest(Map<String, Callable<Quote>> suppliers) throws InterruptedException {
        Map<String, Future<Quote>> asked = new LinkedHashMap<>();
        for (Map.Entry<String, Callable<Quote>> s : suppliers.entrySet()) {
            asked.put(s.getKey(), ask(s.getKey(), s.getValue()));
        }
        Quote best = null;
        for (Map.Entry<String, Future<Quote>> a : asked.entrySet()) {
            Quote q = answer(a.getKey(), a.getValue());
            if (q != null && (best == null || q.cents() < best.cents())) best = q;
        }
        return Optional.ofNullable(best);
    }

    /** Starts asking one supplier on a new thread named "quote-" + supplier, and returns at once. */
    static Future<Quote> ask(String supplier, Callable<Quote> call) {
        // TODO: The suppliers' APIs take up to a few seconds each, and the check must not take as
        //       long as all of them together. Get this supplier's call going, on a thread whose
        //       name on-call can tell apart from the other suppliers', and return without waiting.
        throw new IllegalStateException("TODO: ask");
    }

    /** Waits for one supplier's answer: its quote, or null if it doesn't stock the item. */
    static Quote answer(String supplier, Future<Quote> pending) throws InterruptedException {
        // TODO: Return what the supplier answered. If its call failed, the caller must learn
        //       which supplier it was and what exactly went wrong.
        throw new IllegalStateException("TODO: answer");
    }
}
