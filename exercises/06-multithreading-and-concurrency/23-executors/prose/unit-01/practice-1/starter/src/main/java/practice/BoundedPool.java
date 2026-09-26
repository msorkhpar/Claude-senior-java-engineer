package practice;

import java.util.concurrent.ThreadPoolExecutor;

public final class BoundedPool {

    private BoundedPool() {
    }

    /** A pool of core..max named daemon threads, a queue of queueCapacity, refusing what it cannot take. */
    public static ThreadPoolExecutor create(int core, int max, int queueCapacity, String name) {
        throw new UnsupportedOperationException("write create");
    }
}
