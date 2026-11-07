package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class DeviceTokensTest {

    /**
     * A synchronized list with a pause: when the paused thread looks up the watched token
     * (contains or indexOf), it waits there, after the lookup has returned, until released.
     * The pause sits outside the synchronized list, so the list's own lock is not held during it.
     */
    static final class PausingList extends AbstractList<String> {
        final List<String> inner = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch paused = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        volatile Thread pauseThread;
        volatile String watched;

        private void maybePause(Object o) {
            if (Thread.currentThread() == pauseThread && o != null && o.equals(watched) && paused.getCount() > 0) {
                paused.countDown();
                try {
                    if (!release.await(20, TimeUnit.SECONDS)) throw new IllegalStateException("never released");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override public boolean contains(Object o) { boolean r = inner.contains(o); maybePause(o); return r; }
        @Override public int indexOf(Object o) { int r = inner.indexOf(o); maybePause(o); return r; }
        @Override public String get(int i) { return inner.get(i); }
        @Override public int size() { return inner.size(); }
        @Override public String set(int i, String s) { return inner.set(i, s); }
        @Override public void add(int i, String s) { inner.add(i, s); }
        @Override public String remove(int i) { return inner.remove(i); }
        @Override public boolean remove(Object o) { return inner.remove(o); }
        @Override public Object[] toArray() { return inner.toArray(); }
    }

    static <T> FutureTask<T> startDaemon(String name, Callable<T> body) {
        FutureTask<T> task = new FutureTask<>(body);
        Thread t = new Thread(task, name);
        t.setDaemon(true);
        t.start();
        return task;
    }

    static Thread named(String name) {
        for (Thread t : Thread.getAllStackTraces().keySet()) if (t.getName().equals(name)) return t;
        return null;
    }

    /**
     * Waits until the second caller has either finished or is waiting for a lock the first
     * caller holds. Both mean the test can release the first caller and look at the result.
     */
    static void awaitFinishedOrBlockedBy(FutureTask<?> second, String secondName, Thread first) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (second.isDone()) return;
            Thread t = named(secondName);
            if (t != null) {
                ThreadInfo info = mx.getThreadInfo(t.threadId());
                if (info != null && info.getLockOwnerId() == first.threadId()) return;
            }
            Thread.onSpinWait();
        }
        fail("the second caller neither finished nor waited for the first one's lock within 10 s");
    }

    /** Waits until the caller is paused at its lookup; fails if it finished or failed first. */
    static void awaitPaused(PausingList list, FutureTask<?> caller, String what) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (list.paused.getCount() == 0) return;
            if (caller.isDone()) {
                caller.get();                         // rethrows what the caller threw
                fail("the call finished without the expected lookup: " + what);
            }
            Thread.onSpinWait();
        }
        fail("the call never reached its lookup within 10 s: " + what);
    }

    static long copiesOf(DeviceTokens store, String token) {
        return store.snapshot().stream().filter(token::equals).count();
    }

    @Test
    void registersEachTokenOnce() {
        DeviceTokens store = new DeviceTokens();

        assertThat(store.register("apns-7f3a")).isTrue();
        assertThat(store.register("fcm-19c2")).isTrue();
        assertThat(store.register("apns-7f3a")).isFalse();

        assertThat(store.snapshot()).containsExactly("apns-7f3a", "fcm-19c2");
    }

    @Test
    void rotateReplacesARegisteredToken() {
        DeviceTokens store = new DeviceTokens();
        store.register("fcm-0001");
        store.register("apns-0002");

        assertThat(store.rotate("fcm-0001", "fcm-0003")).isTrue();
        assertThat(store.rotate("fcm-9999", "fcm-0004")).isFalse();

        assertThat(store.snapshot()).containsExactlyInAnyOrder("fcm-0003", "apns-0002");
    }

    @Test
    void twoRegistrationsOfOneTokenAtOnceStoreItOnce() throws Exception {
        PausingList list = new PausingList();
        DeviceTokens store = new DeviceTokens(list);
        list.watched = "wns-55aa";

        FutureTask<Boolean> first = startDaemon("first-register", () -> {
            list.pauseThread = Thread.currentThread();
            return store.register("wns-55aa");
        });
        awaitPaused(list, first, "register() looks up the token with contains or indexOf");
        FutureTask<Boolean> second = startDaemon("second-register", () -> store.register("wns-55aa"));
        awaitFinishedOrBlockedBy(second, "second-register", named("first-register"));
        list.release.countDown();

        boolean a = first.get(10, TimeUnit.SECONDS);
        boolean b = second.get(10, TimeUnit.SECONDS);
        assertThat(copiesOf(store, "wns-55aa")).isEqualTo(1);
        assertThat(a ^ b).as("exactly one registration reports that it added the token").isTrue();
    }

    @Test
    void aRegistrationDuringARotationOntoTheSameTokenStoresItOnce() throws Exception {
        PausingList list = new PausingList();
        DeviceTokens store = new DeviceTokens(list);
        store.register("fcm-old-81");
        list.watched = "fcm-new-82";

        FutureTask<Boolean> rotation = startDaemon("rotation", () -> {
            list.pauseThread = Thread.currentThread();
            return store.rotate("fcm-old-81", "fcm-new-82");
        });
        awaitPaused(list, rotation, "rotate() looks up the new token with contains or indexOf");
        Thread rotationThread = named("rotation");
        FutureTask<Boolean> registration = startDaemon("registration", () -> store.register("fcm-new-82"));
        awaitFinishedOrBlockedBy(registration, "registration", rotationThread);
        list.release.countDown();

        rotation.get(10, TimeUnit.SECONDS);
        registration.get(10, TimeUnit.SECONDS);
        assertThat(copiesOf(store, "fcm-new-82")).isEqualTo(1);
    }

    @Test
    void aRotationOntoATokenAlreadyRegisteredChangesNothing() {
        DeviceTokens store = new DeviceTokens();
        store.register("apns-a1");
        store.register("apns-b2");

        assertThat(store.rotate("apns-a1", "apns-b2")).isFalse();

        assertThat(store.snapshot()).containsExactlyInAnyOrder("apns-a1", "apns-b2");
    }
}
