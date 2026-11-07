package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongUnaryOperator;

/** Nets a day's trades on the ForkJoinPool it is given, in pieces of at most leafSize(rows) trades. */
public class SettlementJob {

    private final ForkJoinPool pool;
    private final int minRows;
    private final LongUnaryOperator net;
    private final AtomicInteger pieces = new AtomicInteger();

    /**
     * @param pool    the pool the job runs in
     * @param minRows a batch of this many trades or fewer is never cut up
     * @param net     the net amount of one trade
     */
    public SettlementJob(ForkJoinPool pool, int minRows, LongUnaryOperator net) {
        this.pool = pool;
        this.minRows = minRows;
        this.net = net;
    }

    /** The day's net total. */
    public long total(long[] trades) {
        pieces.set(0);
        int leaf = Math.max(1, leafSize(trades.length));
        return pool.invoke(new Part(trades, 0, trades.length, leaf));
    }

    /** How many pieces the last run was netted in. */
    public int pieces() {
        return pieces.get();
    }

    /**
     * The largest number of trades one piece nets. A range of at most this many trades is netted
     * in one piece; a longer one is halved.
     */
    int leafSize(int rows) {
        return Math.max(minRows, rows / (Runtime.getRuntime().availableProcessors() * 32));
    }

    private final class Part extends RecursiveTask<Long> {
        private final long[] trades;
        private final int from, to, leaf;

        Part(long[] trades, int from, int to, int leaf) {
            this.trades = trades;
            this.from = from;
            this.to = to;
            this.leaf = leaf;
        }

        @Override
        protected Long compute() {
            if (to - from <= leaf) {
                pieces.incrementAndGet();
                long s = 0;
                for (int i = from; i < to; i++) s += net.applyAsLong(trades[i]);
                return s;
            }
            int mid = (from + to) >>> 1;
            Part left = new Part(trades, from, mid, leaf);
            left.fork();
            long r = new Part(trades, mid, to, leaf).compute();
            return left.join() + r;
        }
    }
}
