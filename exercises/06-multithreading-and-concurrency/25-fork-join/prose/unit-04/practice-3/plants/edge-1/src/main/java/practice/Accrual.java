package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.LongUnaryOperator;

/**
 * Month-end interest accrual: every account balance, in cents, is adjusted. The bank has a few
 * million accounts, so the adjustment runs on a ForkJoinPool. The month-end report reads the
 * balances as soon as accrue() returns. An adjustment can fail on a bad account; the operator then
 * fixes the account and runs the accrual again.
 */
public class Accrual {

    private final ForkJoinPool pool;
    private final int threshold;
    private final LongUnaryOperator adjust;

    /**
     * @param pool      the pool the accrual runs in
     * @param threshold the most balances one task adjusts by itself, on its own thread
     * @param adjust    the new balance of one account, given its old balance
     */
    public Accrual(ForkJoinPool pool, int threshold, LongUnaryOperator adjust) {
        this.pool = pool;
        this.threshold = threshold;
        this.adjust = adjust;
    }

    /**
     * Adjusts every balance of the book. The report reads them as soon as this returns. If any
     * adjustment fails, the failure reaches the caller and the book is left as it was.
     */
    public void accrue(long[] balances) {
        pool.invoke(new Adjust(balances, 0, balances.length));      // in place, as before
    }

    /** Adjusts balances[from] to balances[to - 1]. */
    final class Adjust extends RecursiveAction {
        private final long[] balances;
        private final int from, to;

        Adjust(long[] balances, int from, int to) {
            this.balances = balances;
            this.from = from;
            this.to = to;
        }

        /** Adjusts this task's balances one after another, on the current thread. */
        void adjustHere() {
            for (int i = from; i < to; i++) balances[i] = adjust.applyAsLong(balances[i]);
        }

        @Override
        protected void compute() {
            if (to - from <= threshold) {
                adjustHere();
                return;
            }
            int mid = (from + to) >>> 1;
            invokeAll(new Adjust(balances, from, mid), new Adjust(balances, mid, to));
        }
    }
}
