package practice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

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
        // TODO: Ask every carrier on the pool and build the board's lines as described above,
        //       by the deadline even when a carrier never answers.
        throw new IllegalStateException("TODO: collect");
    }
}
