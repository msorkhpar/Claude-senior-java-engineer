package practice;
import java.util.*;
import java.util.concurrent.*;
public final class Squares {
    private Squares() {}
    public static final class TaskProducer {
        public List<Callable<Long>> createTasks(int count) {
            if (count < 0) throw new IllegalArgumentException();
            List<Callable<Long>> t = new ArrayList<>();
            for (int i = 0; i < count; i++) { final long v = i; t.add(() -> v * v); }
            return t;
        }
    }
    public static final class TaskExecutor {
        private final ExecutorService es;
        public TaskExecutor(ExecutorService es) { this.es = es; }
        public List<Future<Long>> submitAll(List<Callable<Long>> tasks) {
            List<Future<Long>> f = new ArrayList<>();
            for (Callable<Long> c : tasks) f.add(es.submit(c));
            return f;
        }
        public void shutdown() { es.shutdownNow(); }
    }
    public static final class ResultAggregator {
        public long sumResults(List<Future<Long>> futures) throws ExecutionException, InterruptedException {
            long s = 0; for (Future<Long> f : futures) s += f.get(); return s;
        }
    }
}
