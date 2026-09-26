package practice;
import java.util.concurrent.*;
public final class SerialWorker implements AutoCloseable {
    private final ExecutorService executor;
    public SerialWorker(String name) {
        executor = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, name); t.setDaemon(true); return t; });
    }
    public <T> Future<T> submit(Callable<T> task) { return executor.submit(task); }
    @Override public void close() {
        executor.shutdown();
        try { executor.awaitTermination(1, TimeUnit.SECONDS); } // gives up after 1 s
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
