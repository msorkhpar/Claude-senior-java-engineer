package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.DoubleConsumer;

/**
 * Pushes price ticks to the widgets of a trading screen. Each widget subscribes to one symbol
 * when it opens and closes its subscription when the trader closes it.
 */
public class PriceFeed {

    /** Ends one subscription. */
    public interface Subscription extends AutoCloseable {
        @Override
        void close();
    }

    /** One registration: which symbol, and whom to tell. */
    static final class Entry {
        final String symbol;
        final DoubleConsumer listener;

        Entry(String symbol, DoubleConsumer listener) {
            this.symbol = symbol;
            this.listener = listener;
        }
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    /** Tells {@code listener} every price of {@code symbol} from now until the subscription is closed. */
    public Subscription subscribe(String symbol, DoubleConsumer listener) {
        entries.add(new Entry(symbol, listener));
        return () -> entries.removeIf(e -> e.listener == listener);
    }

    public void tick(String symbol, double price) {
        for (Entry e : entries) {
            if (e.symbol.equals(symbol)) e.listener.accept(price);
        }
    }

    public int subscriptionCount() {
        return entries.size();
    }
}
