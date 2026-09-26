package practice;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class Shutdown {

    private Shutdown() {
    }

    /** Shuts the executor down in two phases and returns the tasks that never started. */
    public static List<Runnable> close(ExecutorService executor, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("write close");
    }
}
