package practice;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitoredPool extends ThreadPoolExecutor {

    public MonitoredPool(int threads) {
        super(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    /** The message of every task that failed, from execute() or submit(). */
    public List<String> failures() {
        throw new UnsupportedOperationException("write failures");
    }

    /** How many tasks ended without an exception. */
    public int successes() {
        throw new UnsupportedOperationException("write successes");
    }

    /** Whether the pool has terminated. */
    public boolean finished() {
        throw new UnsupportedOperationException("write finished");
    }
}
