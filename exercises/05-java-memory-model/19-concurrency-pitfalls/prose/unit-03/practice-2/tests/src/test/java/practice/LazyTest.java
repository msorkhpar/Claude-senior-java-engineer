package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LazyTest {

    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    /** Waits, at most 20 s, until t has finished or waits for a lock that owner holds; true if it waits on owner. */
    private static boolean waitsOn(Thread t, Thread owner) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            if (t.getState() == Thread.State.TERMINATED) {
                return false;
            }
            ThreadInfo info = THREADS.getThreadInfo(t.threadId());
            if (info != null && info.getLockOwnerId() == owner.threadId()) {
                return true;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return false;
    }

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void createsOnceAndReturnsTheSameValue() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<StringBuilder> lazy = new Lazy<>(() -> {
            calls.incrementAndGet();
            return new StringBuilder("config");
        });
        assertThat(lazy.isInitialized()).isFalse();
        StringBuilder first = lazy.get();
        assertThat(first).hasToString("config");
        assertThat(lazy.get()).isSameAs(first);
        assertThat(lazy.get()).isSameAs(first);
        assertThat(lazy.isInitialized()).isTrue();
        assertThat(calls.get()).isEqualTo(1);
        assertThatThrownBy(() -> new Lazy<String>(() -> null).get()).isInstanceOf(NullPointerException.class);
    }

    @Test
    void racingFirstCallsShareOneCreation() throws InterruptedException {
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Lazy<Object> lazy = new Lazy<>(() -> {
            if (calls.incrementAndGet() == 1) {
                entered.countDown();
                try {
                    release.await(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return new Object();
        });
        AtomicReference<Object> a = new AtomicReference<>();
        AtomicReference<Object> b = new AtomicReference<>();
        Thread first = daemon(() -> a.set(lazy.get()));
        assertThat(entered.await(20, TimeUnit.SECONDS)).as("the first call reached the factory").isTrue();
        Thread second = daemon(() -> b.set(lazy.get()));
        waitsOn(second, first);
        int callsWhileTheFirstIsBuilding = calls.get();
        release.countDown();
        first.join(20_000);
        second.join(20_000);
        assertThat(first.isAlive() || second.isAlive()).isFalse();
        assertThat(callsWhileTheFirstIsBuilding).as("the second caller waited instead of building").isEqualTo(1);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(b.get()).isSameAs(a.get());
    }

    @Test
    void theCachedFieldIsVolatile() {
        new Lazy<>(Object::new).get();
        List<Field> mutable = Arrays.stream(Lazy.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers()))
                .toList();
        assertThat(mutable).as("a field caches the value").isNotEmpty();
        assertThat(mutable).allSatisfy(f -> assertThat(Modifier.isVolatile(f.getModifiers()))
                .as("field %s is volatile", f.getName()).isTrue());
    }

    @Test
    void aReadyValueTakesNoLock() throws InterruptedException {
        Lazy<String> lazy = new Lazy<>(() -> "ready");
        assertThat(lazy.get()).isEqualTo("ready");
        AtomicReference<String> seen = new AtomicReference<>();
        Thread reader;
        synchronized (lazy) {
            reader = daemon(() -> seen.set(lazy.get()));
            reader.join(20_000);
            assertThat(reader.isAlive()).as("get() returned while another thread held the Lazy's lock").isFalse();
        }
        reader.join(20_000);
        assertThat(seen.get()).isEqualTo("ready");
    }
}
