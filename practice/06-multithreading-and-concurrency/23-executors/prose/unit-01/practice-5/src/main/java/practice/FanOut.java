package practice;

import java.util.List;
import java.util.concurrent.Callable;

public final class FanOut {

    private FanOut() {
    }

    /** Runs every task on its own virtual thread and returns the results in task order. */
    public static <T> List<T> all(List<Callable<T>> tasks) throws Exception {
        throw new UnsupportedOperationException("write all");
    }
}
