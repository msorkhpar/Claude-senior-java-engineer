package practice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

/**
 * The nightly reconciliation. Each shard of the ledger is matched on a thread of its own, and the
 * report is due when the budget runs out, whether or not every shard has finished by then.
 */
public class ShardReport {

    /** Matches one shard of the ledger, passing each matched amount, in cents, to {@code add}. */
    public interface Shard {
        void reconcile(LongConsumer add) throws InterruptedException;
    }

    /** The total of the shards that finished, and the indexes of the shards that didn't. */
    public record Report(long totalCents, List<Integer> unfinished) {
    }

    public static Report run(List<Shard> shards, Duration budget) throws InterruptedException {
        long deadline = System.nanoTime() + budget.toNanos();
        long[] subtotals = new long[shards.size()];           // one plain slot per shard
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < shards.size(); i++) {
            int slot = i;
            Shard shard = shards.get(i);
            Thread t = new Thread(() -> {
                try {
                    shard.reconcile(amount -> subtotals[slot] += amount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "shard-" + i);
            t.setDaemon(true);                                // an overrunning shard can't keep the JVM up
            threads.add(t);
        }
        threads.forEach(Thread::start);
        return collect(threads, subtotals, deadline);
    }

    /**
     * Builds the report from the shards' threads and their subtotals, by the deadline (a
     * {@link System#nanoTime()} value).
     */
    static Report collect(List<Thread> threads, long[] subtotals, long deadline) throws InterruptedException {
        long total = 0;
        List<Integer> unfinished = new ArrayList<>();
        for (int i = 0; i < threads.size(); i++) {
            long millisLeft = Math.max(0, (deadline - System.nanoTime()) / 1_000_000);
            threads.get(i).join(millisLeft);
            if (threads.get(i).isAlive()) unfinished.add(i);
            else total += subtotals[i];
        }
        return new Report(total, unfinished);
    }
}
