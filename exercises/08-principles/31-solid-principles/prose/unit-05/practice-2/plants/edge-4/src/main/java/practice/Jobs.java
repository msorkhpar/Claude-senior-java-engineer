package practice;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
public final class Jobs {
    private Jobs() {}
    public interface ExecutionStrategy { <T> Future<T> execute(Callable<T> task); void shutdown(); }
    public static final class VirtualThreadStrategy implements ExecutionStrategy {
        private final ExecutorService es = Executors.newVirtualThreadPerTaskExecutor();
        public <T> Future<T> execute(Callable<T> task) { return es.submit(task); }
        public void shutdown() { es.shutdown(); }
    }
    public static final class JobScheduler {
        private final ExecutionStrategy strategy; private final AtomicInteger count = new AtomicInteger(); private int plain;
        public JobScheduler(ExecutionStrategy strategy) { this.strategy = Objects.requireNonNull(strategy); }
        public <T> Future<T> scheduleJob(Callable<T> job) { Future<T> f = strategy.execute(job); count.incrementAndGet(); return f; }
        public int getJobCount() { return count.get() + plain; }
        public void shutdown() { strategy.shutdown(); }
    }
}
