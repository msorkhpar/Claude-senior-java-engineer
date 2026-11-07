package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits an inclusive range of record IDs into batches for the reindexer. IDs are ints and the
 * range may reach any int value, Integer.MAX_VALUE included.
 */
public class IdBatches {

    /** Receives one batch: the IDs fromId to toId, both included. */
    public interface Handler {
        void handle(int fromId, int toId);
    }

    /** The batches as "from..to" strings, for the reindexer's plan log. */
    public static List<String> plan(int firstId, int lastId, int size) {
        List<String> plan = new ArrayList<>();
        forEachBatch(firstId, lastId, size, (from, to) -> plan.add(from + ".." + to));
        return plan;
    }

    /**
     * Calls handler once per batch, in order, so that the batches cover firstId to lastId, both
     * included, each holds at most size IDs, and no ID is handed over twice.
     */
    static void forEachBatch(int firstId, int lastId, int size, Handler handler) {
        // TODO: Hand the range to handler batch by batch, in order. The range can end at the
        //       largest int there is.
        throw new IllegalStateException("TODO: forEachBatch");
    }
}
