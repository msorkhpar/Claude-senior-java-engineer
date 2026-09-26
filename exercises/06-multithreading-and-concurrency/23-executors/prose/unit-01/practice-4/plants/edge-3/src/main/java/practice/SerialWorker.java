package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class SerialWorker implements AutoCloseable {

    private final ExecutorService executor;

    public SerialWorker(String name) {
        executor = Executors.newSingleThreadExecutor();
    }

    /** Queues a task; tasks run one at a time, in submission order. */
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /** Accepts no more tasks and returns once every submitted task has run. */
    @Override
    public void close() {
        executor.close();
    }
}
