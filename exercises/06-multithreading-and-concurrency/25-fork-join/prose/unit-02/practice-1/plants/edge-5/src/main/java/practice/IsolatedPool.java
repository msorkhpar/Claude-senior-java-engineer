package practice;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
public final class IsolatedPool {
    private IsolatedPool() {}
    public static <T> T run(ForkJoinTask<T> task, int parallelism) {
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        T result;
        try {
            result = pool.invoke(task);
        } catch (RuntimeException e) {
            pool.shutdown();
            throw e;
        }
        pool.shutdown();
        return result;
    }
}
