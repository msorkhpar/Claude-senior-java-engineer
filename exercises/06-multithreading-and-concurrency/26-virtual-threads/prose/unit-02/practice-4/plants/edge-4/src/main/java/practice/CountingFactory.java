package practice;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class CountingFactory implements ThreadFactory {
    private static final AtomicInteger RUNNING = new AtomicInteger();
    private final Thread.Builder builder;

    public CountingFactory(String prefix) {
        this.builder = Thread.ofVirtual().name(prefix, 0);
    }

    @Override
    public Thread newThread(Runnable task) {
        return builder.unstarted(() -> {
            RUNNING.incrementAndGet();
            try {
                task.run();
            } finally {
                RUNNING.decrementAndGet();
            }
        });
    }

    public int running() {
        return RUNNING.get();
    }
}
