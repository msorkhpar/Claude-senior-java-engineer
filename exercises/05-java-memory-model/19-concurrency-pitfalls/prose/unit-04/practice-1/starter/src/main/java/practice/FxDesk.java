package practice;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The quoting desk. Its book of rates is not thread-safe and belongs to the desk's own thread:
 * every call into the book runs there, and nothing else synchronizes it. Quotes come from the
 * market-data adapters as futures, completed later on an adapter's thread, or already complete
 * when the adapter had the rate cached.
 */
public class FxDesk {

    /** The desk's book of rates. Not thread-safe. */
    public interface Book {
        void apply(String pair, long micros);

        /** The pair's rate in millionths, or null if it was never quoted. */
        Long rate(String pair);
    }

    private final Executor deskThread;
    private final Book book;

    public FxDesk(Executor deskThread, Book book) {
        this.deskThread = deskThread;
        this.book = book;
    }

    /** Puts the quote into the book once it has arrived. Returns at once. */
    public void onQuote(String pair, CompletableFuture<Long> quote) {
        // TODO: Once the quote has arrived, apply it to the book. The book belongs to the desk's
        //       thread, whether the quote is already here or arrives later on an adapter's thread.
        throw new IllegalStateException("TODO: onQuote");
    }

    /** The book's current rate for the pair (null if never quoted), once the book has answered. */
    public CompletableFuture<Long> rate(String pair) {
        // TODO: Answer from the book. The book belongs to the desk's thread, whoever asks.
        throw new IllegalStateException("TODO: rate");
    }
}
