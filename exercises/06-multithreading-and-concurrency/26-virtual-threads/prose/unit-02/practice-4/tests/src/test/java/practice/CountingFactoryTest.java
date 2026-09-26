package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class CountingFactoryTest {

    /** Polls until the condition holds or 5 s pass; says whether it held. */
    private static boolean eventually(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return true;
    }

    /** Submits three tasks that record their thread and block until the gate opens. */
    private static Set<Thread> runThree(CountingFactory factory, CountDownLatch gate, Consumer<Set<Thread>> whileBlocked) {
        Set<Thread> seen = ConcurrentHashMap.newKeySet();
        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(factory)) {
            try {
                for (int i = 0; i < 3; i++) {
                    executor.submit(() -> {
                        seen.add(Thread.currentThread());
                        gate.await(8, TimeUnit.SECONDS);
                        return null;
                    });
                }
                whileBlocked.accept(seen);
            } finally {
                gate.countDown();
            }
        }
        return seen;
    }

    @Test
    void countsTheThreadsThatAreRunning() {
        CountingFactory factory = new CountingFactory("order-");
        CountDownLatch gate = new CountDownLatch(1);
        Set<Thread> seen = runThree(factory, gate, blocked -> {
            assertThat(eventually(() -> factory.running() == 3 && blocked.size() == 3))
                    .as("running() while three tasks block").isTrue();
            assertThat(Thread.getAllStackTraces().keySet())
                    .as("getAllStackTraces() leaves virtual threads out").doesNotContainAnyElementsOf(blocked);
        });
        assertThat(seen).hasSize(3);
        // the executor counts a task done just before the factory's wrapper does, so allow a moment
        assertThat(eventually(() -> factory.running() == 0)).as("running() after the executor closed").isTrue();
    }

    @Test
    void makesVirtualThreadsWithThePrefix() {
        CountingFactory factory = new CountingFactory("order-");
        Set<Thread> seen = runThree(factory, new CountDownLatch(1), blocked -> { });
        assertThat(seen).hasSize(3).allSatisfy(t -> {
            assertThat(t.isVirtual()).isTrue();
            assertThat(t.getName()).startsWith("order-");
        });
    }

    @Test
    void namesCountFromZero() {
        CountingFactory factory = new CountingFactory("order-");
        List<String> names = List.of(
                factory.newThread(() -> { }).getName(),
                factory.newThread(() -> { }).getName(),
                factory.newThread(() -> { }).getName());
        assertThat(names).containsExactly("order-0", "order-1", "order-2");
    }

    @Test
    void anUnstartedThreadIsNotCounted() {
        CountingFactory factory = new CountingFactory("order-");
        factory.newThread(() -> { });
        factory.newThread(() -> { });
        assertThat(factory.running()).isZero();
    }

    @Test
    void aTaskThatThrowsIsNoLongerCounted() throws InterruptedException {
        CountingFactory factory = new CountingFactory("order-");
        Thread t = factory.newThread(() -> {
            throw new IllegalStateException("order rejected");
        });
        t.setUncaughtExceptionHandler((thread, e) -> { });
        t.start();
        t.join(5_000);
        assertThat(t.isAlive()).isFalse();
        assertThat(factory.running()).isZero();
    }
}
