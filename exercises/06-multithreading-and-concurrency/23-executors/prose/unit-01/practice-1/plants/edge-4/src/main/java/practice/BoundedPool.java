package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*;
public final class BoundedPool {
    private BoundedPool() {}
    public static ThreadPoolExecutor create(int core, int max, int queueCapacity, String name) {
        AtomicInteger n = new AtomicInteger();
        ThreadPoolExecutor p = new ThreadPoolExecutor(core, max, 50, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueCapacity),
                r -> { Thread t = new Thread(r, name + "-" + n.incrementAndGet()); t.setDaemon(true); return t; });
        p.allowCoreThreadTimeOut(true); // core threads die when idle
        return p;
    }
}
