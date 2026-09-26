package practice;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class Roles {

    private Roles() {
    }

    public interface TaskSubmitter<T> {
        Future<T> submit(Callable<T> task);
    }

    public interface LifecycleManageable {
        void shutdown();

        boolean isShutdown();
    }

    public interface Monitorable {
        long getCompletedCount();
    }

    /** One class plays every role. */
    public static final class ManagedExecutor implements TaskSubmitter<Object>, LifecycleManageable, Monitorable {
        private final ExecutorService pool;
        private final AtomicBoolean shutdown = new AtomicBoolean(false);
        private final AtomicLong completed = new AtomicLong();

        public ManagedExecutor(int poolSize) {
            this.pool = Executors.newFixedThreadPool(poolSize);
        }

        public Future<Object> submit(Callable<Object> task) {
            if (shutdown.get()) {
                throw new IllegalStateException("the executor is shut down");
            }
            return pool.submit(() -> {
                try {
                    return task.call();
                } finally {
                    completed.incrementAndGet();
                }
            });
        }

        public void shutdown() {
            shutdown.set(true);
            pool.shutdown();
        }

        public boolean isShutdown() {
            return shutdown.get();
        }

        public long getCompletedCount() {
            return completed.get();
        }
    }

    /** Only submits tasks, so it depends only on the submitter role. */
    public static final class TaskClient {
        private final ManagedExecutor submitter;

        public TaskClient(ManagedExecutor submitter) {
            this.submitter = Objects.requireNonNull(submitter);
        }

        public Future<Object> runTask(Callable<Object> task) {
            return submitter.submit(task);
        }
    }

    /** Only manages the lifecycle, so it depends only on the lifecycle role. */
    public static final class LifecycleManager {
        private final LifecycleManageable manageable;

        public LifecycleManager(LifecycleManageable manageable) {
            this.manageable = Objects.requireNonNull(manageable);
        }

        public void gracefulShutdown() {
            manageable.shutdown();
        }

        public boolean isRunning() {
            return !manageable.isShutdown();
        }
    }
}
