package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BoundedPoolTest {

    private final CountDownLatch release = new CountDownLatch(1);
    private final Set<String> names = ConcurrentHashMap.newKeySet();
    private final Set<Boolean> daemons = ConcurrentHashMap.newKeySet();
    private ThreadPoolExecutor pool;

    /** A task that records its thread's name, then stays busy until the test releases it (at most 5 s). */
    private Runnable busy(CountDownLatch started) {
        return () -> {
            names.add(Thread.currentThread().getName());
            daemons.add(Thread.currentThread().isDaemon());
            started.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    @AfterEach
    void stop() throws InterruptedException {
        release.countDown();
        if (pool != null) {
            pool.shutdownNow();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void fillsCoreThreadsThenTheQueue() {
        pool = BoundedPool.create(2, 4, 2, "orders");
        CountDownLatch started = new CountDownLatch(4);
        for (int i = 0; i < 4; i++) {
            pool.execute(busy(started));
        }
        assertThat(pool.getPoolSize()).as("threads after 4 tasks").isEqualTo(2);
        assertThat(pool.getQueue()).as("queued after 4 tasks").hasSize(2);
    }

    @Test
    void growsPastCoreOnlyWhenTheQueueIsFull() {
        pool = BoundedPool.create(2, 4, 2, "orders");
        CountDownLatch started = new CountDownLatch(6);
        for (int i = 0; i < 6; i++) {
            pool.execute(busy(started));
        }
        assertThat(pool.getPoolSize()).as("threads after 6 tasks").isEqualTo(4);
        assertThat(pool.getQueue()).as("queued after 6 tasks").hasSize(2);
    }

    @Test
    void rejectsWhenPoolAndQueueAreFull() {
        pool = BoundedPool.create(2, 4, 2, "orders");
        CountDownLatch started = new CountDownLatch(6);
        for (int i = 0; i < 6; i++) {
            pool.execute(busy(started));
        }
        assertThatThrownBy(() -> pool.execute(() -> { }))
                .as("a 7th task on a full pool and a full queue")
                .isInstanceOf(RejectedExecutionException.class);
    }

    @Test
    void namesItsThreads() throws Exception {
        ThreadPoolExecutor earlier = BoundedPool.create(1, 1, 1, "orders");
        try {
            earlier.submit(() -> { }).get(5, TimeUnit.SECONDS);
        } finally {
            earlier.shutdownNow();
        }
        pool = BoundedPool.create(2, 4, 2, "orders");
        CountDownLatch started = new CountDownLatch(4);
        for (int i = 0; i < 6; i++) {
            pool.execute(busy(started));
        }
        assertThat(started.await(5, TimeUnit.SECONDS)).as("four tasks started").isTrue();
        assertThat(names).as("each pool numbers its own threads from 1")
                .containsExactlyInAnyOrder("orders-1", "orders-2", "orders-3", "orders-4");
        assertThat(daemons).as("every pool thread is a daemon").containsExactly(true);
    }

    @Test
    void coreThreadsAreKept() throws Exception {
        pool = BoundedPool.create(2, 4, 2, "keep");
        assertThat(pool.getCorePoolSize()).isEqualTo(2);
        assertThat(pool.getMaximumPoolSize()).isEqualTo(4);
        assertThat(pool.allowsCoreThreadTimeOut()).as("core threads are kept, not timed out when idle").isFalse();
    }
}
