package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
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
            return pool.invoke(new Total(rows, 0, rows.length, new AtomicBoolean()));
        } catch (IllegalArgumentException e) {
            throw original(e);
        }
    }

    /** The exception that was thrown in the task, given the one that reached this thread. */
    static IllegalArgumentException original(IllegalArgumentException caught) {
        Throwable e = caught;
        while (e.getCause() != null && e.getCause().getClass() == caught.getClass()) e = e.getCause();
        return (IllegalArgumentException) e;
    }

    /** The total of rows from to to - 1. */
    final class Total extends RecursiveTask<Long> {
        private final String[] rows;
        private final int from, to;
        private final AtomicBoolean failed;

        Total(String[] rows, int from, int to, AtomicBoolean failed) {
            this.rows = rows;
            this.from = from;
            this.to = to;
            this.failed = failed;
        }

        @Override
        protected Long compute() {
            if (failed.get()) return 0L;
            if (to - from <= threshold) {
                long cents = 0;
                for (int i = from; i < to; i++) {
                    try {
                        cents += parseCents.applyAsLong(rows[i]);
                    } catch (NumberFormatException e) {
                        failed.set(true);
                        throw new IllegalArgumentException("row " + i + ": " + e.getMessage(), e);
                    }
                }
                return cents;
            }
            int mid = (from + to) >>> 1;
            Total left = new Total(rows, from, mid, failed);
            left.fork();
            long right = new Total(rows, mid, to, failed).compute();
            return left.join() + right;
        }
    }
}
