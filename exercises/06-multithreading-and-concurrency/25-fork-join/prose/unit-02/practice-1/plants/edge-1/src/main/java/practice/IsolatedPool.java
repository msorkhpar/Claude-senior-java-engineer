package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public final class IsolatedPool {

    private IsolatedPool() {
    }

    /** Runs the task in a new pool of the given parallelism, returns its result, and always shuts the pool down. */
    public static <T> T run(ForkJoinTask<T> task, int parallelism) {
        ForkJoinPool pool = new ForkJoinPool(); // default parallelism
        try {
            return pool.invoke(task);
        } finally {
            pool.shutdown();
        }
    }
}
