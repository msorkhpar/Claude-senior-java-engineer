package practice;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
public final class Roles {
    private Roles() {}
    public interface TaskSubmitter<T> { Future<T> submit(Callable<T> task); }
    public interface LifecycleManageable { void shutdown(); boolean isShutdown(); }
    public interface Monitorable { long getCompletedCount(); }
    public static final class ManagedExecutor implements TaskSubmitter<Object>, LifecycleManageable, Monitorable {
        private final ExecutorService pool; private final AtomicBoolean down = new AtomicBoolean();
        private final AtomicLong done = new AtomicLong();
        public ManagedExecutor(int poolSize) { pool = Executors.newFixedThreadPool(poolSize); }
        public Future<Object> submit(Callable<Object> task) { if (down.get()) throw new IllegalStateException("shut down"); return pool.submit(() -> { Object r = task.call(); done.incrementAndGet(); return r; }); }
        public void shutdown() { down.set(true); pool.shutdown(); }
        public boolean isShutdown() { return down.get(); }
        public long getCompletedCount() { return done.get(); }
    }
    public static final class TaskClient {
        private final TaskSubmitter<Object> s;
        public TaskClient(TaskSubmitter<Object> s) { this.s = s; }
        public Future<Object> runTask(Callable<Object> task) { return s.submit(task); }
    }
    public static final class LifecycleManager {
        private final LifecycleManageable m;
        public LifecycleManager(LifecycleManageable m) { this.m = m; }
        public void gracefulShutdown() { m.shutdown(); }
        public boolean isRunning() { return !m.isShutdown(); }
    }
}
