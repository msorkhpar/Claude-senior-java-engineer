package practice;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
public final class Batch {
    private Batch() {}
    public static <T> List<T> runAll(ForkJoinPool pool, List<? extends ForkJoinTask<T>> tasks) {
        List<ForkJoinTask<T>> submitted = tasks.stream().<ForkJoinTask<T>>map(pool::submit).toList();
        return submitted.stream().map(ForkJoinTask::join).distinct().toList();
    }
}
