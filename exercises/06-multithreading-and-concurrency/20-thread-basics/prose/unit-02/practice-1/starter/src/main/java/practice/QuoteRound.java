package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Asks suppliers for a price, each on its own thread, for the price-comparison page. The page
 * renders the quotes as soon as askAll returns.
 */
public class QuoteRound {

    /** Asks one supplier for a price; blocks for as long as the supplier takes to answer. */
    public interface PriceDesk {
        int quote(String supplier);
    }

    /** Receives each supplier's price. Called on the thread that asked that supplier. */
    public interface Quotes {
        void record(String supplier, int cents);
    }

    private final PriceDesk desk;

    public QuoteRound(PriceDesk desk) {
        this.desk = desk;
    }

    /** Asks every supplier and returns once every supplier's price has been recorded. */
    public void askAll(List<String> suppliers, Quotes quotes) throws InterruptedException {
        // TODO: Ask each supplier on its own thread (askerFor builds that thread). The page should
        //       take about as long as the slowest supplier, not the sum of them all, and when this
        //       method returns, every supplier's price must already be in quotes.
        throw new IllegalStateException("TODO: askAll");
    }

    /** A thread, not started yet, that asks one supplier and records its price. */
    Thread askerFor(String supplier, Quotes quotes) {
        return new Thread(() -> quotes.record(supplier, desk.quote(supplier)), "quote-" + supplier);
    }
}
