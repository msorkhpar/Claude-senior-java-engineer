package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LaunchTest {

    private static List<Thread> startThree(CountDownLatch ran) {
        Runnable task = ran::countDown;
        return Launch.startAll("job-", 7, List.of(task, task, task));
    }

    private static void joinAll(List<Thread> threads) throws InterruptedException {
        for (Thread t : threads) {
            t.join(5_000);
        }
    }

    @Test
    void startsEveryTask() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(3);
        List<Thread> threads = startThree(ran);
        assertThat(threads).hasSize(3);
        assertThat(ran.await(5, TimeUnit.SECONDS)).as("every task ran").isTrue();
        joinAll(threads);
    }

    @Test
    void awaitAllReportsNoStragglers() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(3);
        List<Thread> threads = startThree(ran);
        joinAll(threads);
        assertThat(Launch.awaitAll(threads, Duration.ofSeconds(5))).isEmpty();
    }

    /** Each thread runs for about 2 s (a latch nobody opens, awaited with a 2 s timeout). */
    @Test
    void awaitAllWaitsForRunningThreads() throws InterruptedException {
        CountDownLatch never = new CountDownLatch(1);
        Runnable twoSeconds = () -> {
            try {
                never.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        List<Thread> threads = Launch.startAll("slow-", 0, List.of(twoSeconds, twoSeconds));
        assertThat(Launch.awaitAll(threads, Duration.ofSeconds(6))).as("threads reported still alive").isEmpty();
        assertThat(threads).allSatisfy(t -> assertThat(t.isAlive()).isFalse());
    }

    @Test
    void threadsAreVirtual() throws InterruptedException {
        List<Thread> threads = startThree(new CountDownLatch(3));
        assertThat(threads).allSatisfy(t -> assertThat(t.isVirtual()).as(t.getName()).isTrue());
        joinAll(threads);
    }

    @Test
    void namesCountFromFirst() throws InterruptedException {
        List<Thread> threads = startThree(new CountDownLatch(3));
        assertThat(threads).extracting(Thread::getName).containsExactly("job-7", "job-8", "job-9");
        joinAll(threads);
    }

    /**
     * Five threads that never end and a 1 s limit: one shared deadline returns after about 1 s, a limit per
     * thread needs at least 5 s. The test allows 4 s.
     */
    @Test
    void awaitAllSharesOneDeadline() throws InterruptedException {
        CountDownLatch release = new CountDownLatch(1);
        List<Thread> stuck = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            stuck.add(Thread.ofVirtual().start(() -> {
                try {
                    release.await(9, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        AtomicReference<List<Thread>> left = new AtomicReference<>();
        Thread caller = Thread.ofPlatform().daemon(true).start(() -> {
            try {
                left.set(Launch.awaitAll(stuck, Duration.ofSeconds(1)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            caller.join(4_000);
            assertThat(caller.isAlive()).as("awaitAll waited the limit once per thread").isFalse();
            assertThat(left.get()).containsExactlyElementsOf(stuck);
        } finally {
            release.countDown();
        }
    }

    /** Runs awaitAll on its own thread, so a wrong solution that waits forever cannot hang the test. */
    @Test
    void awaitAllGivesUpOnAStuckThread() throws InterruptedException {
        CountDownLatch release = new CountDownLatch(1);
        Thread stuck = Thread.ofVirtual().start(() -> {
            try {
                release.await(8, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread done = Thread.ofVirtual().start(() -> { });
        AtomicReference<List<Thread>> left = new AtomicReference<>();
        Thread caller = Thread.ofPlatform().daemon(true).start(() -> {
            try {
                left.set(Launch.awaitAll(List.of(done, stuck), Duration.ofMillis(200)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            caller.join(5_000);
            assertThat(caller.isAlive()).as("awaitAll kept waiting past its limit").isFalse();
            assertThat(left.get()).containsExactly(stuck);
        } finally {
            release.countDown();
        }
    }

    @Test
    void awaitAllReturnsOnceAllHaveEnded() throws Exception {
        CountDownLatch done = new CountDownLatch(2);
        List<Thread> threads = Launch.startAll("quick-", 0, List.of(done::countDown, done::countDown));
        assertThat(done.await(3, TimeUnit.SECONDS)).isTrue();
        long start = System.nanoTime();
        List<Thread> alive = Launch.awaitAll(threads, Duration.ofSeconds(5));
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(alive).isEmpty();
        assertThat(tookMs).as("threads that have already finished are not waited on for the whole limit").isLessThan(3_000);
    }

    @Test
    void theLimitIsSharedNotSplit() throws Exception {
        Runnable twoSeconds = () -> {
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        Runnable instant = () -> { };
        List<Thread> threads = Launch.startAll("share-", 0, List.of(twoSeconds, instant, instant, instant));
        List<Thread> alive = Launch.awaitAll(threads, Duration.ofSeconds(4));
        assertThat(alive).as("one 4 s limit shared by all: the 2 s thread is waited for, not cut off at a quarter of it").isEmpty();
    }
}
