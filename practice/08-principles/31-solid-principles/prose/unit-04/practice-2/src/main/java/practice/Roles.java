package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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

        public ManagedExecutor(int poolSize) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public Future<Object> submit(Callable<Object> task) {
            throw new UnsupportedOperationException("write submit");
        }

        public void shutdown() {
            throw new UnsupportedOperationException("write shutdown");
        }

        public boolean isShutdown() {
            throw new UnsupportedOperationException("write isShutdown");
        }

        public long getCompletedCount() {
            throw new UnsupportedOperationException("write getCompletedCount");
        }
    }

    /** Only submits tasks. Choose the one role it depends on. */
    public static final class TaskClient {

        public TaskClient(ManagedExecutor executor) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public Future<Object> runTask(Callable<Object> task) {
            throw new UnsupportedOperationException("write runTask");
        }
    }

    /** Only manages the lifecycle. Choose the one role it depends on. */
    public static final class LifecycleManager {

        public LifecycleManager(ManagedExecutor executor) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public void gracefulShutdown() {
            throw new UnsupportedOperationException("write gracefulShutdown");
        }

        public boolean isRunning() {
            throw new UnsupportedOperationException("write isRunning");
        }
    }
}
