package practice;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class QuotasTest {

    /**
     * A ConcurrentHashMap that holds one chosen thread right after its first read (get,
     * getOrDefault or containsKey), until the test lets it go. Calls that read and write in one
     * step never pass through here, so they are never held.
     */
    static final class HoldingMap extends ConcurrentHashMap<String, Integer> {
        volatile Thread target;
        final CountDownLatch settled = new CountDownLatch(1);   // the target is held, or has finished
        final CountDownLatch resume = new CountDownLatch(1);
        private final AtomicBoolean held = new AtomicBoolean();

        private void holdIfTarget() {
            if (Thread.currentThread() == target && held.compareAndSet(false, true)) {
                settled.countDown();
                try {
                    resume.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public Integer get(Object key) {
            Integer v = super.get(key);
            holdIfTarget();
            return v;
        }

        @Override
        public Integer getOrDefault(Object key, Integer defaultValue) {
            Integer v = super.getOrDefault(key, defaultValue);
            holdIfTarget();
            return v;
        }

        @Override
        public boolean containsKey(Object key) {
            boolean b = super.containsKey(key);
            holdIfTarget();
            return b;
        }
    }

    private static <T> T within(ExecutorService pool, java.util.concurrent.Callable<T> call) throws Exception {
        Future<T> f = pool.submit(call);
        return f.get(5, TimeUnit.SECONDS);
    }

    private static ExecutorService daemons() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Runs first on a thread that the map holds after its first read, then second to the end on
     * another thread, then lets the first go on. Returns the first call's result.
     */
    private static <T> T interleave(HoldingMap store, java.util.concurrent.Callable<T> first,
                                    java.util.concurrent.Callable<?> second) throws Exception {
        ExecutorService pool = daemons();
        try {
            AtomicReference<T> firstResult = new AtomicReference<>();
            Future<?> a = pool.submit(() -> {
                store.target = Thread.currentThread();
                try {
                    firstResult.set(first.call());
                } finally {
                    store.settled.countDown();
                }
                return null;
            });
            assertThat(store.settled.await(5, TimeUnit.SECONDS)).as("the first call was held or finished").isTrue();
            within(pool, second);
            store.resume.countDown();
            a.get(5, TimeUnit.SECONDS);
            return firstResult.get();
        } finally {
            store.resume.countDown();
            pool.shutdownNow();
        }
    }

    private static void grantNow(Quotas quotas, String client, int calls) throws Exception {
        ExecutorService pool = daemons();
        try {
            within(pool, () -> {
                quotas.grant(client, calls);
                return null;
            });
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void grantsAddUpAndEachCallTakesOne() throws Exception {
        ExecutorService pool = daemons();
        try {
            Quotas quotas = new Quotas();
            within(pool, () -> {
                quotas.grant("k-7731", 2);
                quotas.grant("k-7731", 1);
                return null;
            });
            assertThat(quotas.remaining("k-7731")).isEqualTo(3);
            assertThat(within(pool, () -> quotas.tryUse("k-7731"))).isTrue();
            assertThat(within(pool, () -> quotas.tryUse("k-7731"))).isTrue();
            assertThat(within(pool, () -> quotas.tryUse("k-7731"))).isTrue();
            assertThat(within(pool, () -> quotas.tryUse("k-7731"))).isFalse();
            assertThat(quotas.remaining("k-7731")).isZero();
            assertThat(within(pool, () -> quotas.tryUse("k-0000"))).as("a client never granted anything").isFalse();
            assertThat(quotas.remaining("k-0000")).isZero();
            within(pool, () -> {
                quotas.grant("k-7731", 4);
                return null;
            });
            assertThat(quotas.remaining("k-7731")).isEqualTo(4);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void twoTopUpsAtTheSameMomentBothCount() throws Exception {
        HoldingMap store = new HoldingMap();
        Quotas quotas = new Quotas(store);
        grantNow(quotas, "k-5150", 40);

        interleave(store, () -> {
            quotas.grant("k-5150", 25);
            return null;
        }, () -> {
            quotas.grant("k-5150", 100);
            return null;
        });

        assertThat(quotas.remaining("k-5150")).isEqualTo(165);
    }

    @Test
    void theLastCallGoesToExactlyOneOfTwoRequests() throws Exception {
        HoldingMap store = new HoldingMap();
        Quotas quotas = new Quotas(store);
        grantNow(quotas, "k-2048", 1);
        AtomicBoolean secondGotIt = new AtomicBoolean();

        boolean firstGotIt = interleave(store, () -> quotas.tryUse("k-2048"),
                () -> {
                    secondGotIt.set(quotas.tryUse("k-2048"));
                    return null;
                });

        assertThat(firstGotIt ^ secondGotIt.get()).as("exactly one request got the last call").isTrue();
        assertThat(quotas.remaining("k-2048")).isZero();
    }
}
