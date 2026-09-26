package practice;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class BoundedPool {

    private BoundedPool() {
    }

    /** A pool of core..max named daemon threads, a queue of queueCapacity, refusing what it cannot take. */
    public static ThreadPoolExecutor create(int core, int max, int queueCapacity, String name) {
        AtomicInteger created = new AtomicInteger();
        ThreadFactory factory = task -> {
            Thread thread = new Thread(task, name + "-" + created.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return new ThreadPoolExecutor(core, max, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), factory, new ThreadPoolExecutor.AbortPolicy());
    }
}
