package practice;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
public final class TaskRunners {
    private TaskRunners() {}
    public interface TaskRunner { String run(String input) throws Exception; }
    public static final class UpperCaseRunner implements TaskRunner { @Override public String run(String input) { return input.toUpperCase(); } }
    public static final class LoggingTaskRunner implements TaskRunner {
        private final TaskRunner delegate; private final List<String> log;
        public LoggingTaskRunner(TaskRunner delegate, List<String> log) { this.delegate = Objects.requireNonNull(delegate); this.log = Objects.requireNonNull(log); }
        @Override public String run(String input) throws Exception { log.add("START: " + input); try { String r = delegate.run(input); log.add("SUCCESS: " + r); return r; } catch (Exception e) { log.add("ERROR: " + e.getMessage()); throw e; } }
    }
    public static final class SynchronizedTaskRunner implements TaskRunner {
        private final TaskRunner delegate; private static final ReentrantLock lock = new ReentrantLock();
        public SynchronizedTaskRunner(TaskRunner delegate) { this.delegate = Objects.requireNonNull(delegate); }
        @Override public String run(String input) throws Exception { lock.lock(); try { return delegate.run(input); } finally { lock.unlock(); } }
    }
    public static TaskRunner compose(TaskRunner base, List<String> log, boolean logging, boolean threadSafe) { TaskRunner r = base; if (logging) r = new LoggingTaskRunner(r, log); if (threadSafe) r = new SynchronizedTaskRunner(r); return r; }
}
