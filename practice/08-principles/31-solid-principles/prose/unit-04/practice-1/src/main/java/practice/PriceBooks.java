package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/** The shop's price books: the live one, and frozen month-end copies that finance replays orders against. */
public final class PriceBooks {

    private PriceBooks() {
    }

    /** Answers prices, in cents. */
    public interface PriceLookup {
        long priceOf(String sku);

        Set<String> skus();
    }

    /** Changes prices. */
    public interface PriceEditor {
        void setPrice(String sku, long cents);
    }

    /** A book that can be read and changed. */
    public interface PriceBook extends PriceLookup, PriceEditor {
    }

    /** The prices the shop charges today. */
    public static final class LivePriceBook implements PriceBook {
        private final Map<String, Long> prices = new HashMap<>();

        @Override
        public synchronized long priceOf(String sku) {
            Long cents = prices.get(sku);
            if (cents == null) throw new NoSuchElementException("no price for " + sku);
            return cents;
        }

        @Override
        public synchronized Set<String> skus() {
            return Set.copyOf(prices.keySet());
        }

        @Override
        public synchronized void setPrice(String sku, long cents) {
            prices.put(sku, cents);
        }
    }

    /**
     * The prices as they stood when the book was frozen. They never change afterwards.
     *
     * TODO: Last month-end, the repricing job was started against a frozen book by mistake. It
     *       compiled, it passed review, and it failed at run time on the first price it tried to
     *       change. Make that mistake one the compiler catches, while finance's Checkout keeps
     *       running on frozen books. (This class, and Checkout below.)
     */
    public static final class FrozenPriceBook implements PriceBook {
        private final Map<String, Long> prices;

        private FrozenPriceBook(Map<String, Long> prices) {
            this.prices = prices;
        }

        /** Freezes the prices the source answers right now. */
        public static FrozenPriceBook of(PriceLookup source) {
            // TODO: Take the prices the source answers now. Repricing the source afterwards must
            //       not change what this book answers.
            throw new IllegalStateException("TODO: of");
        }

        @Override
        public long priceOf(String sku) {
            Long cents = prices.get(sku);
            if (cents == null) throw new NoSuchElementException("no price for " + sku);
            return cents;
        }

        @Override
        public Set<String> skus() {
            return prices.keySet();
        }

        @Override
        public void setPrice(String sku, long cents) {
            throw new UnsupportedOperationException("a frozen price book can't change");
        }
    }

    /** Totals a basket of sku -> quantity. Finance also runs it on frozen books to replay old orders. */
    public static final class Checkout {
        private final PriceBook prices;

        public Checkout(PriceBook prices) {
            this.prices = prices;
        }

        public long total(Map<String, Integer> basket) {
            long cents = 0;
            for (Map.Entry<String, Integer> line : basket.entrySet()) {
                cents += prices.priceOf(line.getKey()) * line.getValue();
            }
            return cents;
        }
    }

    /** The month-end job: takes a percentage off every price. */
    public static final class Repricer {
        private final PriceBook book;

        public Repricer(PriceBook book) {
            this.book = book;
        }

        public void discount(int percent) {
            for (String sku : book.skus()) {
                book.setPrice(sku, book.priceOf(sku) * (100 - percent) / 100);
            }
        }
    }
}
