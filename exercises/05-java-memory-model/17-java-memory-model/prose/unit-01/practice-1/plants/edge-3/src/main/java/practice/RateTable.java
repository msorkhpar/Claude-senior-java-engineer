package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Exchange rates for the checkout, in millionths of a euro: a USD rate of 921_400 means
 * 1 USD = 0.9214 EUR. The table is loaded from the rate store on first use, and again on the
 * first lookup after every refresh().
 */
public class RateTable {

    /** The rate store. A full load streams every rate to the sink and takes a few seconds. */
    public interface RateStore {
        void load(BiConsumer<String, Long> sink);
    }

    private final RateStore store;
    private volatile Map<String, Long> table;          // null until loaded, and again after refresh()

    public RateTable(RateStore store) {
        this.store = store;
    }

    /** The rate of one currency. */
    public long rate(String currency) {
        Long rate = table().get(currency);
        if (rate == null) throw new IllegalArgumentException("no rate for " + currency);
        return rate;
    }

    /** Every rate, for the end-of-day report. */
    public Map<String, Long> rates() {
        return table();
    }

    /** Called when the store has new rates: the next lookup loads them. */
    public void refresh() {
        table = null;
    }

    /** The current table, loaded if there is none. */
    Map<String, Long> table() {
        Map<String, Long> t = table;
        if (t == null) {
            t = load();
            table = t;
        }
        return t;
    }

    /** One full load from the store. */
    Map<String, Long> load() {
        Map<String, Long> rates = new HashMap<>();
        store.load(rates::put);
        return rates;
    }
}
