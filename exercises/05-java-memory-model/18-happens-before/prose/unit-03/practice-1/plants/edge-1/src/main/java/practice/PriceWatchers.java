package practice;

import java.util.function.LongConsumer;

/**
 * Price alerts for the trading desk. Each watcher follows one symbol on the market-data feed and
 * raises alerts; the desk writes its own watchers by extending {@link PriceWatcher}.
 */
public class PriceWatchers {

    /** The market-data feed. Prices are in cents. */
    public interface PriceFeed {
        /**
         * Delivers every later price of {@code symbol} to {@code listener}. If the symbol has
         * traded already, the listener is first handed the last price, at once, on the calling
         * thread, so a new subscriber doesn't wait for the next trade to know where the price is.
         */
        void subscribe(String symbol, LongConsumer listener);
    }

    /** Where watchers send their alerts. */
    public interface Alerts {
        void raise(String alert);
    }

    /** The base class of every watcher. */
    public abstract static class PriceWatcher {
        private final String symbol;
        private final Alerts alerts;
        private final PriceFeed feed;

        protected PriceWatcher(String symbol, Alerts alerts, PriceFeed feed) {
            this.symbol = symbol;
            this.alerts = alerts;
            this.feed = feed;
            feed.subscribe(symbol, this::onPrice);       // the last line: every field is set
        }

        public final String symbol() {
            return symbol;
        }

        protected final void alert(String text) {
            alerts.raise(text);
        }

        /**
         * The desk calls this once the watcher is built. From then on the watcher sees every price
         * of its symbol, starting with the last one.
         */
        public PriceWatcher start() {
            return this;
        }

        /** One price of this watcher's symbol, in cents. */
        protected abstract void onPrice(long cents);
    }

    /** Alerts whenever the price is above a ceiling. */
    public static final class Ceiling extends PriceWatcher {
        private final long ceilingCents;

        public Ceiling(String symbol, long ceilingCents, Alerts alerts, PriceFeed feed) {
            super(symbol, alerts, feed);
            this.ceilingCents = ceilingCents;
        }

        @Override
        protected void onPrice(long cents) {
            if (cents > ceilingCents) {
                alert(symbol() + " at " + cents + " is above " + ceilingCents);
            }
        }
    }

    private PriceWatchers() {
    }
}
