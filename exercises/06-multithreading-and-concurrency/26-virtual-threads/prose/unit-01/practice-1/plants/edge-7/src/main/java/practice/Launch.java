package practice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class Launch {
    private Launch() {
    }
    public static List<Thread> startAll(String prefix, long first, List<Runnable> tasks) {
        Thread.Builder builder = Thread.ofVirtual().name(prefix, first);
        List<Thread> threads = new ArrayList<>();
        for (Runnable task : tasks) {
            threads.add(builder.start(task));
        }
        return threads;
    }
    public static List<Thread> awaitAll(List<Thread> threads, Duration limit) throws InterruptedException {
        long each = limit.toNanos() / Math.max(1, threads.size());
        List<Thread> alive = new ArrayList<>();
        for (Thread t : threads) {
            t.join(Duration.ofNanos(each));
            if (t.isAlive()) {
                alive.add(t);
            }
        }
        return alive;
    }
}
