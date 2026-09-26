package practice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class Gather {

    private Gather() {
    }

    /** One result per task, in task order; a failed or unfinished task gives the fallback. */
    public static <T> List<T> all(ExecutorService executor, List<Callable<T>> tasks,
                                  long timeout, TimeUnit unit, T fallback) throws InterruptedException {
        throw new UnsupportedOperationException("write all");
    }
}
