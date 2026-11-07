package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.ToLongFunction;

/**
 * Imports a ledger file: parses every row's amount and totals them, on a ForkJoinPool. A file
 * has millions of rows, and one bad row makes the whole file wrong.
 */
public class LedgerImport {

    private final ForkJoinPool pool;
    private final int threshold;
    private final ToLongFunction<String> parseCents;

    /**
     * @param pool       the pool the import runs in
     * @param threshold  the most rows one task parses by itself on its own thread
     * @param parseCents parses one row's amount into cents; throws NumberFormatException,
     *                   with a message that says why, for a row that is not an amount
     */
    public LedgerImport(ForkJoinPool pool, int threshold, ToLongFunction<String> parseCents) {
        this.pool = pool;
        this.threshold = threshold;
        this.parseCents = parseCents;
    }

    /**
     * The total of every row, in cents.
     *
     * @throws IllegalArgumentException for a file with a bad row, with the message
     *         {@code "row " + index + ": " + the parser's message}
     */
    public long total(String[] rows) {
        try {
            return pool.invoke(new Total(rows, 0, rows.length));
        } catch (IllegalArgumentException e) {
            throw original(e);
        }
    }

    /** The exception that was thrown in the task, given the one that reached this thread. */
    static IllegalArgumentException original(IllegalArgumentException caught) {
        // TODO: Return the IllegalArgumentException that a task threw, so that the import's
        //       caller sees that one's message.
        throw new IllegalStateException("TODO: original");
    }

    /** The total of rows from to to - 1. */
    final class Total extends RecursiveTask<Long> {
        private final String[] rows;
        private final int from, to;

        Total(String[] rows, int from, int to) {
            this.rows = rows;
            this.from = from;
            this.to = to;
        }

        @Override
        protected Long compute() {
            // TODO: Return the total of this task's rows; up to `threshold` rows are parsed right
            //       here, more are shared with the pool's other workers. A row the parser refuses
            //       ends the import with IllegalArgumentException("row " + index + ": " + the
            //       parser's message). Once a row is bad, the rest of the file isn't worth parsing.
            //       You may give Total more fields and constructor parameters.
            throw new IllegalStateException("TODO: compute");
        }
    }
}
