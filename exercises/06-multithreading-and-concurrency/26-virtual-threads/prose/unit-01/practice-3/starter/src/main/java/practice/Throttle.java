package practice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

public final class Throttle {

    private Throttle() {
    }

    /** Runs each task on its own virtual thread while it holds a permit; results in task order. */
    public static <T> List<T> runAll(List<Callable<T>> tasks, Semaphore permits)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("write runAll");
    }
}
