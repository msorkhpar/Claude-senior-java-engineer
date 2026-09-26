package practice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class Launch {

    private Launch() {
    }

    /** Starts one named virtual thread per task, in task order, and returns them. */
    public static List<Thread> startAll(String prefix, long first, List<Runnable> tasks) {
        Thread.Builder builder = Thread.ofVirtual().name(prefix, first);
        List<Thread> threads = new ArrayList<>();
        for (Runnable task : tasks) {
            threads.add(builder.start(task));
        }
        return threads;
    }

    /** Waits at most about {@code limit} in total and returns the threads still alive. */
    public static List<Thread> awaitAll(List<Thread> threads, Duration limit) throws InterruptedException {
        long deadline = System.nanoTime() + limit.toNanos();
        List<Thread> alive = new ArrayList<>();
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                alive.add(thread);
            }
        }
        return alive;
    }
}
