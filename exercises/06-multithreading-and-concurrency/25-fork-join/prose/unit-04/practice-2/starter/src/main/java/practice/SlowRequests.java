package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Scans a day of request latencies for requests slower than the service's target, and reports
 * them to the operations dashboard's tally. A day has tens of millions of requests, so the scan
 * runs on a ForkJoinPool.
 */
public class SlowRequests {

    /** The dashboard's running totals. Every report takes the tally's lock. */
    public static final class Tally {
        private long requests, millis;
        private int reports;

        /** Adds slow requests and the milliseconds they took to the totals. */
        public synchronized void report(long slowRequests, long slowMillis) {
            requests += slowRequests;
            millis += slowMillis;
            reports++;
        }

        public synchronized long requests() { return requests; }

        public synchronized long millis() { return millis; }

        /** How many times report() was called; the dashboard's lock is held once per call. */
        public synchronized int reports() { return reports; }
    }

    private final ForkJoinPool pool;
    private final int threshold;
    private final int targetMillis;

    /**
     * @param pool         the pool the scan runs in
     * @param threshold    the most latencies one task scans by itself, on its own thread
     * @param targetMillis a request is slow when it took longer than this
     */
    public SlowRequests(ForkJoinPool pool, int threshold, int targetMillis) {
        this.pool = pool;
        this.threshold = threshold;
        this.targetMillis = targetMillis;
    }

    /** Reports every slow request of the day to the tally, and returns once all are reported. */
    public void scan(int[] latencies, Tally tally) {
        pool.invoke(new Scan(latencies, 0, latencies.length, tally));
    }

    /** Scans latencies[from] to latencies[to - 1]. */
    final class Scan extends RecursiveAction {
        private final int[] latencies;
        private final int from, to;
        private final Tally tally;

        Scan(int[] latencies, int from, int to, Tally tally) {
            this.latencies = latencies;
            this.from = from;
            this.to = to;
            this.tally = tally;
        }

        @Override
        protected void compute() {
            if (to - from <= threshold) {
                scanHere();
                return;
            }
            int mid = (from + to) >>> 1;
            invokeAll(new Scan(latencies, from, mid, tally), new Scan(latencies, mid, to, tally));
        }

        /** Reports the slow requests of this task's range, scanned on the current thread. */
        void scanHere() {
            // TODO: Report every request of this task's range that took longer than targetMillis,
            //       and the milliseconds those requests took, to the tally. Every worker of the
            //       scan reports to the same tally.
            throw new IllegalStateException("TODO: scanHere");
        }
    }
}
