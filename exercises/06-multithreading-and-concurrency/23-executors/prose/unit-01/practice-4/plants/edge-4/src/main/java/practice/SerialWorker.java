package practice;
import java.util.concurrent.*;
public final class SerialWorker implements AutoCloseable {
    private final ThreadPoolExecutor executor;
    public SerialWorker(String name) {
        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                r -> { Thread t = new Thread(r, name); t.setDaemon(true); return t; });
        executor.prestartAllCoreThreads(); // starts its thread at construction
    }
    public <T> Future<T> submit(Callable<T> task) { return executor.submit(task); }
    @Override public void close() { executor.close(); }
}
