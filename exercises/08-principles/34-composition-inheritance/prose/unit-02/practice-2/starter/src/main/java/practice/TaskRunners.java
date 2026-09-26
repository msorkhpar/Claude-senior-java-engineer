package practice;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/** Task runners composed from decorators instead of a deep class hierarchy. */
public final class TaskRunners {

    private TaskRunners() {
    }

    public interface TaskRunner {
        String run(String input) throws Exception;
    }

    /** The base runner: pure business logic. */
    public static final class UpperCaseRunner implements TaskRunner {
        @Override
        public String run(String input) {
            return input.toUpperCase();
        }
    }

    /** Logs "START: input", then "SUCCESS: result" or "ERROR: message", around its delegate. */
    public static final class LoggingTaskRunner implements TaskRunner {
        private final TaskRunner delegate;
        private final List<String> log;

        public LoggingTaskRunner(TaskRunner delegate, List<String> log) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
            this.log = Objects.requireNonNull(log, "log");
        }

        @Override
        public String run(String input) throws Exception {
            throw new UnsupportedOperationException("write LoggingTaskRunner.run");
        }
    }

    /** Lets one call at a time into its delegate. */
    public static final class SynchronizedTaskRunner implements TaskRunner {
        private final TaskRunner delegate;
        private final ReentrantLock lock = new ReentrantLock();

        public SynchronizedTaskRunner(TaskRunner delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public String run(String input) throws Exception {
            throw new UnsupportedOperationException("write SynchronizedTaskRunner.run");
        }
    }

    /** Wraps base in the decorators asked for: logging inside, the lock outermost. */
    public static TaskRunner compose(TaskRunner base, List<String> log, boolean logging, boolean threadSafe) {
        throw new UnsupportedOperationException("write compose");
    }
}
