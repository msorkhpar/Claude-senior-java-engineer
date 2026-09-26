package practice;
import java.util.concurrent.*;
public final class SerialWorker implements AutoCloseable {
    private final ExecutorService executor;
    public SerialWorker(String name) {
        executor = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, name); t.setDaemon(true); return t; });
    }
    public <T> Future<T> submit(Callable<T> task) { return executor.submit(task); }
    @Override public void close() {
        // waits for the queue to drain, but never stops accepting tasks
        try { executor.submit(() -> null).get(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (ExecutionException e) { throw new IllegalStateException(e); }
    }
}
