package practice;

import java.time.Duration;
import java.util.List;

public final class Launch {

    private Launch() {
    }

    /** Starts one named virtual thread per task, in task order, and returns them. */
    public static List<Thread> startAll(String prefix, long first, List<Runnable> tasks) {
        throw new UnsupportedOperationException("write startAll");
    }

    /** Waits at most about {@code limit} in total and returns the threads still alive. */
    public static List<Thread> awaitAll(List<Thread> threads, Duration limit) throws InterruptedException {
        throw new UnsupportedOperationException("write awaitAll");
    }
}
