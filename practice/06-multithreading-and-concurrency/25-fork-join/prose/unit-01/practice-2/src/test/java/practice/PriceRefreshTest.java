package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class PriceRefreshTest {

    private static final List<String> SKUS = List.of("KTL-0042", "MUG-1187", "TEA-3301", "CUP-0900", "PAN-7712",
            "LID-2045", "JAR-5150");

    /** A made-up rate: deterministic per SKU. */
    private static long rate(String sku) {
        return 100L + Math.floorMod(sku.hashCode(), 9_000);
    }

    private static Map<String, Long> expected(List<String> skus) {
        Map<String, Long> m = new java.util.HashMap<>();
        for (String s : skus) m.put(s, rate(s));
        return m;
    }

    /** Runs the call on a daemon thread and waits for it at most 30 seconds. */
    private static <T> T within30s(Callable<T> call, Set<Thread> callers) throws Exception {
        FutureTask<T> f = new FutureTask<>(call);
        Thread t = new Thread(f, "test-caller");
        t.setDaemon(true);
        callers.add(t);
        t.start();
        try {
            return f.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception x) throw x;
            throw e;
        }
    }

    @Test
    void returnsThePriceOfEverySku() throws Exception {
        PriceRefresh refresh = new PriceRefresh(PriceRefreshTest::rate, 3);

        assertThat(within30s(() -> refresh.refresh(SKUS), ConcurrentHashMap.newKeySet())).isEqualTo(expected(SKUS));
        assertThat(within30s(() -> refresh.refresh(List.of()), ConcurrentHashMap.newKeySet())).isEmpty();
    }

    @Test
    void lookupsRunOnThreadsNobodyElseShares() throws Exception {
        Set<Thread> lookupThreads = ConcurrentHashMap.newKeySet();
        Set<Thread> callers = ConcurrentHashMap.newKeySet();
        PriceRefresh refresh = new PriceRefresh(sku -> {
            lookupThreads.add(Thread.currentThread());
            return rate(sku);
        }, 3);

        assertThat(within30s(() -> refresh.refresh(SKUS), callers)).isEqualTo(expected(SKUS));

        for (Thread t : lookupThreads) {
            assertThat(callers).as("a lookup ran on the thread that called refresh()").doesNotContain(t);
            assertThat(t).as("lookup thread %s", t.getName()).isInstanceOf(ForkJoinWorkerThread.class);
            assertThat(((ForkJoinWorkerThread) t).getPool())
                    .as("a lookup ran in the JVM's common pool").isNotSameAs(ForkJoinPool.commonPool());
        }
    }

    @Test
    void twoLookupsAreInFlightAtTheSameTime() throws Exception {
        // the first and the last SKU wait for each other: they meet only if lookups overlap
        List<String> skus = List.of("OIL-0101", "SALT-202", "RICE-303", "BEAN-404", "FLOUR-55", "HONEY-66");
        CyclicBarrier meet = new CyclicBarrier(2);
        PriceRefresh refresh = new PriceRefresh(sku -> {
            if (sku.equals("OIL-0101") || sku.equals("HONEY-66")) {
                try {
                    meet.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("interrupted while waiting for another lookup", e);
                } catch (BrokenBarrierException | TimeoutException e) {
                    throw new IllegalStateException("no other lookup was in flight", e);
                }
            }
            return rate(sku);
        }, 2);

        assertThat(within30s(() -> refresh.refresh(skus), ConcurrentHashMap.newKeySet())).isEqualTo(expected(skus));
    }
}
