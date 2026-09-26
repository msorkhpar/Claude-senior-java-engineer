package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public final class Batch {

    private Batch() {
    }

    /** Submits every task to the pool before joining any, then returns their results in list order. */
    public static <T> List<T> runAll(ForkJoinPool pool, List<? extends ForkJoinTask<T>> tasks) {
        List<ForkJoinTask<T>> submitted = new ArrayList<>();
        for (ForkJoinTask<T> task : tasks) {
            submitted.add(pool.submit(task));
        }
        List<T> results = new ArrayList<>();
        for (ForkJoinTask<T> task : submitted) {
            results.add(task.join());
        }
        return results;
    }
}
