package practice;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The pool the order service runs its background work on: request handlers submit tasks to it,
 * and housekeeping jobs hand it work through execute(). Every log line carries the id of the
 * request the current thread is serving, read from REQUEST_ID.
 */
public class RequestPool extends ThreadPoolExecutor {

    /** The id of the request the current thread is serving, or null when it serves none. */
    public static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    /**
     * A pool of {@code threads} threads and a queue of {@code queueCapacity} tasks. When both are
     * full, the thread that submits a task runs it itself.
     */
    public RequestPool(int threads, int queueCapacity) {
        super(threads, threads, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueCapacity), r -> {
            Thread t = new Thread(r, "request-pool");
            t.setDaemon(true);
            return t;
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        return carrying(task);
    }

    /** The task, made to run under the request id of the thread that submits it. */
    <T> RunnableFuture<T> carrying(Callable<T> task) {
        String id = REQUEST_ID.get();
        return new FutureTask<>(() -> {
            String previous = REQUEST_ID.get();
            REQUEST_ID.set(id);
            try {
                return task.call();
            } finally {
                REQUEST_ID.set(previous);
            }
        });
    }
}
