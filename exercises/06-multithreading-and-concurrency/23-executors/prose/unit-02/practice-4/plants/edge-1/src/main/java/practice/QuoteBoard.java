package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The freight comparison board. Every carrier is asked for a quote at once, and the board shows
 * what it has when its deadline comes: the quotes that arrived, and a note for each carrier that
 * failed or was too slow.
 */
public class QuoteBoard {

    private final ExecutorService pool;
    private final long deadlineMillis;

    public QuoteBoard(ExecutorService pool, long deadlineMillis) {
        this.pool = pool;
        this.deadlineMillis = deadlineMillis;
    }

    /**
     * One line per carrier, in the map's order: "swiftline: 18400" for a quote,
     * "bluecargo: no quote" for a carrier whose call failed, and "redroute: too late" for a carrier that
     * had not answered when the deadline of deadlineMillis, counted from this call, came.
     */
    public List<String> collect(Map<String, Callable<Long>> carriers) throws InterruptedException {
        List<String> names = new ArrayList<>(carriers.keySet());
        List<Future<Long>> quotes = pool.invokeAll(new ArrayList<>(carriers.values()));
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            lines.add(names.get(i) + ": " + describe(quotes.get(i)));
        }
        return lines;
    }

    private static String describe(Future<Long> quote) throws InterruptedException {
        try {
            return String.valueOf(quote.get());
        } catch (ExecutionException e) {
            return "no quote";
        } catch (CancellationException e) {
            return "too late";
        }
    }
}
