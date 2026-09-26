package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import practice.Caches.BoundedCache;
import practice.Caches.Cache;
import practice.Caches.SimpleCache;
import practice.Caches.ThreadSafeCache;

import static org.assertj.core.api.Assertions.*;

class CachesTest {

    /** A cache whose put of "slow" waits inside until released; it records any overlapping call. */
    static final class GateCache implements Cache<String, Integer> {
        final Cache<String, Integer> store = new SimpleCache<>();
        final CountDownLatch slowInside = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger active = new AtomicInteger();
        final AtomicBoolean overlapped = new AtomicBoolean();
        final AtomicBoolean secondEntered = new AtomicBoolean();

        private void enter() {
            if (active.incrementAndGet() > 1) {
                overlapped.set(true);
            }
        }

        /** Runs one quick call: it marks that a second caller got in, and records any overlap. */
        private <R> R quick(java.util.function.Supplier<R> call) {
            enter();
            try {
                secondEntered.set(true);
                return call.get();
            } finally {
                active.decrementAndGet();
            }
        }

        @Override
        public Integer get(String key) {
            return quick(() -> store.get(key));
        }

        @Override
        public void put(String key, Integer value) {
            enter();
            try {
                if (key.equals("slow")) {
                    slowInside.countDown();
                    release.await();
                }
                store.put(key, value);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            } finally {
                active.decrementAndGet();
            }
        }

        @Override public Integer remove(String key) { return quick(() -> store.remove(key)); }
        @Override public int size() { return quick(store::size); }
        @Override public boolean containsKey(String key) { return quick(() -> store.containsKey(key)); }
    }

    /** True once t waits on a java.util.concurrent lock, or is blocked entering a monitor in the practice code. */
    static boolean parkedOnALock(Thread t) {
        Thread.State state = t.getState();
        StackTraceElement[] stack = t.getStackTrace();
        if (state == Thread.State.BLOCKED) {
            return stack.length > 0 && stack[0].getClassName().startsWith("practice.");
        }
        if (state != Thread.State.WAITING && state != Thread.State.TIMED_WAITING) {
            return false;
        }
        for (StackTraceElement e : stack) {
            if (e.getClassName().startsWith("java.util.concurrent.locks.")) {
                return true;
            }
        }
        return false;
    }

    static Thread start(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    static String s(String v) {
        return new String(v);
    }

    @Test
    void aBoundedCacheEvictsTheOldestKey() throws Exception {
        Cache<String, Integer> cache = new BoundedCache<>(new SimpleCache<>(), 2);
        cache.put(s("a"), 1001);
        cache.put(s("b"), 1002);
        cache.put(s("c"), 1003);
        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.containsKey(s("a"))).isFalse();
        assertThat(cache.get(s("b"))).isEqualTo(1002);
        assertThat(cache.get(s("c"))).isEqualTo(1003);
        Cache<String, Integer> safe = new ThreadSafeCache<>(new SimpleCache<>());
        safe.put(s("k"), 2000);
        assertThat(safe.get(s("k"))).isEqualTo(2000);
        assertThat(safe.containsKey(s("k"))).isTrue();
        assertThat(safe.remove(s("k"))).isEqualTo(2000);
        assertThat(safe.size()).isZero();
    }

    @Test
    void readingAKeyKeepsItFresh() throws Exception {
        Cache<String, Integer> cache = new BoundedCache<>(new SimpleCache<>(), 2);
        cache.put(s("a"), 1001);
        cache.put(s("b"), 1002);
        assertThat(cache.get(s("a"))).isEqualTo(1001);
        cache.put(s("c"), 1003);
        assertThat(cache.containsKey(s("a"))).isTrue();
        assertThat(cache.containsKey(s("b"))).isFalse();
        Cache<String, Integer> misses = new BoundedCache<>(new SimpleCache<>(), 2);
        misses.put(s("a"), 1001);
        assertThat(misses.get(s("x"))).isNull();
        misses.put(s("b"), 1002);
        misses.put(s("c"), 1003);
        misses.put(s("d"), 1004);
        assertThat(misses.size()).as("reading absent keys must not disturb eviction").isEqualTo(2);
        assertThat(misses.containsKey(s("c"))).isTrue();
        assertThat(misses.containsKey(s("d"))).isTrue();
    }

    @Test
    void writingAKeyAgainKeepsItFresh() throws Exception {
        Cache<String, Integer> cache = new BoundedCache<>(new SimpleCache<>(), 2);
        cache.put(s("a"), 1001);
        cache.put(s("b"), 1002);
        cache.put(s("a"), 2001);
        assertThat(cache.size()).isEqualTo(2);
        cache.put(s("c"), 1003);
        assertThat(cache.get(s("a"))).isEqualTo(2001);
        assertThat(cache.containsKey(s("b"))).isFalse();
    }

    @Test
    void aRemovedKeyLeavesTheEvictionOrder() throws Exception {
        Cache<String, Integer> cache = new BoundedCache<>(new SimpleCache<>(), 2);
        cache.put(s("a"), 1001);
        cache.put(s("b"), 1002);
        assertThat(cache.remove(s("a"))).isEqualTo(1001);
        cache.put(s("c"), 1003);
        cache.put(s("d"), 1004);
        cache.put(s("e"), 1005);
        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.containsKey(s("d"))).isTrue();
        assertThat(cache.containsKey(s("e"))).isTrue();
    }

    @Test
    void theThreadSafeWrapperLetsOneCallInAtATime() throws Exception {
        GateCache gate = new GateCache();
        Cache<String, Integer> safe = new ThreadSafeCache<>(gate);
        safe.put(s("warm"), 1000);
        safe.get(s("warm"));
        safe.containsKey(s("warm"));
        safe.size();
        safe.remove(s("warm"));
        Thread writer = start(() -> safe.put(s("slow"), 3000));
        List<Thread> callers = new ArrayList<>();
        try {
            assertThat(gate.slowInside.await(60, TimeUnit.SECONDS)).isTrue();
            List<Runnable> calls = List.of(() -> safe.get(s("slow")), () -> safe.remove(s("other")),
                    () -> safe.size(), () -> safe.containsKey(s("slow")));
            String[] names = {"get", "remove", "size", "containsKey"};
            for (int i = 0; i < calls.size(); i++) {
                gate.secondEntered.set(false);
                Thread caller = start(calls.get(i));
                callers.add(caller);
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
                while (!gate.secondEntered.get() && !parkedOnALock(caller)) {
                    if (System.nanoTime() > deadline) {
                        throw new AssertionError("the " + names[i] + " call neither ran nor waited");
                    }
                    Thread.onSpinWait();
                }
                assertThat(gate.overlapped.get()).as("a %s ran while a put was inside the cache", names[i]).isFalse();
            }
        } finally {
            gate.release.countDown();
        }
        writer.join(60_000);
        for (Thread caller : callers) {
            caller.join(60_000);
        }
        assertThat(safe.get(s("slow"))).isEqualTo(3000);
    }
}
