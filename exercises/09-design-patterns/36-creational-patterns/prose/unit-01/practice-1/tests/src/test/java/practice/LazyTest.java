package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LazyTest {

    @Test
    void createsOnceAndReusesTheValue() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<StringBuilder> lazy = Lazy.of(() -> {
            calls.incrementAndGet();
            return new StringBuilder("pool");
        });

        StringBuilder first = lazy.get();
        StringBuilder second = lazy.get();

        assertThat(first.toString()).isEqualTo("pool");
        assertThat(second).isSameAs(first);
        assertThat(calls).hasValue(1);
    }

    @Test
    void createsNothingBeforeTheFirstGet() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Object> lazy = Lazy.of(() -> {
            calls.incrementAndGet();
            return new Object();
        });

        assertThat(calls).hasValue(0);
        lazy.get();
        assertThat(calls).hasValue(1);
    }

    @Test
    void racingFirstCallsCreateOneValue() throws Exception {
        assertOneCreationWhenARaceIsForced();
    }

    @Test
    void theLockedCheckIsRepeated() throws Exception {
        assertOneCreationWhenARaceIsForced();
    }

    /**
     * Thread A enters the supplier and is held there. Thread B then calls get(): a correct holder
     * makes B wait for the lock; a broken one lets B call the supplier itself. Only then is A released.
     */
    private static void assertOneCreationWhenARaceIsForced() throws Exception {
        CountDownLatch firstInside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        Lazy<Object> lazy = Lazy.of(() -> {
            if (calls.incrementAndGet() == 1) {
                firstInside.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return new Object();
        });
        AtomicReference<Object> seenByA = new AtomicReference<>();
        AtomicReference<Object> seenByB = new AtomicReference<>();

        Thread a = new Thread(() -> seenByA.set(lazy.get()));
        a.start();
        firstInside.await();
        Thread b = new Thread(() -> seenByB.set(lazy.get()));
        b.start();
        while (calls.get() < 2 && !waitsForALock(b)) {
            Thread.onSpinWait();
        }
        release.countDown();
        a.join();
        b.join();

        assertThat(calls).hasValue(1);
        assertThat(seenByB.get()).isNotNull().isSameAs(seenByA.get());
    }

    private static boolean waitsForALock(Thread t) {
        Thread.State s = t.getState();
        return s == Thread.State.BLOCKED || s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING;
    }

    @Test
    void theFieldHoldingTheValueIsVolatile() {
        java.lang.reflect.Field[] valueFields = java.util.Arrays.stream(Lazy.class.getDeclaredFields())
                .filter(f -> !java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == Object.class)
                .toArray(java.lang.reflect.Field[]::new);

        assertThat(valueFields).as("a field of type T").isNotEmpty();
        assertThat(valueFields).allMatch(f -> java.lang.reflect.Modifier.isVolatile(f.getModifiers()));
    }
}
