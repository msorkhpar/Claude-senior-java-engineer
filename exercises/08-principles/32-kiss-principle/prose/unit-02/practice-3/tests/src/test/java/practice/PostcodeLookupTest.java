package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostcodeLookupTest {

    private final ExecutorService first = daemonPool("checkout-1");
    private final ExecutorService second = daemonPool("checkout-2");
    private final AtomicInteger loads = new AtomicInteger();

    private static ExecutorService daemonPool(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }

    @AfterEach
    void stopThreads() {
        first.shutdownNow();
        second.shutdownNow();
    }

    /** A new table object on every load, so two loads give two different tables. */
    private Map<String, String> freshTable() {
        Map<String, String> table = new HashMap<>();
        table.put("AB10", "north");
        table.put("LS1", "north-east");
        table.put("TR18", "south-west");
        return table;
    }

    /** What the second caller got, when it asked while the first load was still running. */
    private record Overlap(Map<String, String> firstGot, Map<String, String> secondGot) { }

    /**
     * The first load starts a second caller and keeps loading until that caller has returned
     * or is blocked on a lock held by the loading thread (5 s at most). Then the load finishes.
     */
    private Overlap overlap() throws Exception {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        AtomicReference<PostcodeLookup> lookup = new AtomicReference<>();
        AtomicReference<Future<Map<String, String>>> secondCall = new AtomicReference<>();
        CountDownLatch secondReturned = new CountDownLatch(1);
        AtomicReference<Long> secondThread = new AtomicReference<>();
        Supplier<Map<String, String>> loader = () -> {
            if (loads.incrementAndGet() == 1) {
                secondCall.set(second.submit(() -> {
                    secondThread.set(Thread.currentThread().threadId());
                    try {
                        return lookup.get().table();
                    } finally {
                        secondReturned.countDown();
                    }
                }));
                long me = Thread.currentThread().threadId();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                try {
                    while (System.nanoTime() < deadline && !secondReturned.await(1, TimeUnit.MILLISECONDS)) {
                        Long id = secondThread.get();
                        ThreadInfo info = id == null ? null : threads.getThreadInfo(id);
                        if (info != null && info.getLockOwnerId() == me) break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return freshTable();
        };
        lookup.set(new PostcodeLookup(loader));
        Map<String, String> firstGot = first.submit(() -> lookup.get().table()).get(10, TimeUnit.SECONDS);
        Map<String, String> secondGot = secondCall.get().get(10, TimeUnit.SECONDS);
        return new Overlap(firstGot, secondGot);
    }

    @Test
    void fetchesTheTableOnFirstUseAndKeepsIt() throws Exception {
        PostcodeLookup lookup = new PostcodeLookup(() -> {
            loads.incrementAndGet();
            return freshTable();
        });
        assertThat(loads.get()).as("loads before the first call").isZero();

        String region = first.submit(() -> lookup.region("LS1")).get(5, TimeUnit.SECONDS);
        Map<String, String> once = first.submit(lookup::table).get(5, TimeUnit.SECONDS);
        Map<String, String> again = first.submit(lookup::table).get(5, TimeUnit.SECONDS);

        assertThat(region).isEqualTo("north-east");
        assertThat(again).isSameAs(once);
        assertThat(loads.get()).isEqualTo(1);
    }

    @Test
    void aCallerArrivingDuringTheLoadGetsTheLoadedTable() throws Exception {
        Overlap overlap = overlap();

        assertThat(overlap.secondGot()).isNotNull().containsEntry("TR18", "south-west");
    }

    @Test
    void aCallerArrivingDuringTheLoadDoesNotStartAnotherLoad() throws Exception {
        Overlap overlap = overlap();

        assertThat(loads.get()).isEqualTo(1);
        assertThat(overlap.secondGot()).isSameAs(overlap.firstGot());
    }

    @Test
    void aLoadThatFailedIsTriedAgainOnTheNextCall() throws Exception {
        PostcodeLookup lookup = new PostcodeLookup(() -> {
            if (loads.incrementAndGet() == 1) throw new IllegalArgumentException("address service timed out");
            return freshTable();
        });

        Future<Map<String, String>> failed = first.submit(lookup::table);
        assertThatThrownBy(() -> failed.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(IllegalArgumentException.class);
        String region = first.submit(() -> lookup.region("AB10")).get(5, TimeUnit.SECONDS);

        assertThat(region).isEqualTo("north");
        assertThat(loads.get()).isEqualTo(2);
    }
}
