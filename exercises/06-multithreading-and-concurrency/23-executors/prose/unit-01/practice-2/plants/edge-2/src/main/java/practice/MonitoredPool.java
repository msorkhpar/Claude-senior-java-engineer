package practice;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitoredPool extends ThreadPoolExecutor {

    private final List<String> failures = new CopyOnWriteArrayList<>();
    private final AtomicInteger successes = new AtomicInteger();
    private volatile boolean finished;

    public MonitoredPool(int threads) {
        super(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    protected void afterExecute(Runnable task, Throwable thrown) {
        super.afterExecute(task, thrown);
        if (thrown == null && task instanceof Future<?> future && future.isDone()) {
            try {
                future.get();
            } catch (ExecutionException e) {
                thrown = e;
            } catch (CancellationException e) {
                thrown = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (thrown == null) {
            successes.incrementAndGet();
        } else {
            failures.add(thrown.getMessage());
        }
    }

    @Override
    protected void terminated() {
        super.terminated();
        finished = true;
    }

    /** The message of every task that failed, from execute() or submit(). */
    public List<String> failures() {
        return List.copyOf(failures);
    }

    /** How many tasks ended without an exception. */
    public int successes() {
        return successes.get();
    }

    /** Whether the pool has terminated. */
    public boolean finished() {
        return finished;
    }
}
