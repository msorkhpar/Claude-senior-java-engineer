package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ConfigStoreTest {

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
    void replacesWholeSnapshots() {
        ConfigStore.AppConfig initial = new ConfigStore.AppConfig("localhost", 8080, false, 3);
        ConfigStore.AppConfig moved = initial.withHost("db").withPort(5432);
        assertThat(initial).isEqualTo(new ConfigStore.AppConfig("localhost", 8080, false, 3));
        assertThat(moved).isEqualTo(new ConfigStore.AppConfig("db", 5432, false, 3));

        ConfigStore store = new ConfigStore(initial);
        assertThat(store.get()).isSameAs(initial);
        ConfigStore.AppConfig stored = store.update(c -> c.withPort(9090));
        assertThat(stored).isEqualTo(new ConfigStore.AppConfig("localhost", 9090, false, 3));
        assertThat(store.get()).isEqualTo(stored);
        store.set(moved);
        assertThat(store.get()).isSameAs(moved);
        assertThatThrownBy(() -> store.set(null)).isInstanceOf(NullPointerException.class);
        assertThat(store.get()).isSameAs(moved);
        assertThat(new ConfigStore.AppConfig("h", 0, true, 0).port()).isZero();
        assertThat(new ConfigStore.AppConfig("h", 65535, true, 0).port()).isEqualTo(65535);
    }

    @Test
    void invalidConfigsAreRefused() {
        assertThatThrownBy(() -> new ConfigStore.AppConfig(null, 80, false, 0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ConfigStore.AppConfig("h", -1, false, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConfigStore.AppConfig("h", 70000, false, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConfigStore.AppConfig("h", 80, false, -1)).isInstanceOf(IllegalArgumentException.class);
        ConfigStore.AppConfig ok = new ConfigStore.AppConfig("h", 80, false, 0);
        assertThatThrownBy(() -> ok.withPort(65536)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void racingUpdatesAreBothKept() throws InterruptedException {
        ConfigStore store = new ConfigStore(new ConfigStore.AppConfig("localhost", 8080, false, 3));
        CountDownLatch aHasRead = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger aCalls = new AtomicInteger();
        Thread a = daemon(() -> store.update(c -> {
            if (aCalls.incrementAndGet() == 1) {
                aHasRead.countDown();
                try {
                    release.await(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return c.withHost("db");
        }));
        assertThat(aHasRead.await(20, TimeUnit.SECONDS)).isTrue();
        Thread b = daemon(() -> store.update(c -> c.withPort(5432)));
        waitsOn(b, a);
        release.countDown();
        a.join(20_000);
        b.join(20_000);
        assertThat(a.isAlive() || b.isAlive()).isFalse();
        assertThat(store.get()).isEqualTo(new ConfigStore.AppConfig("db", 5432, false, 3));
    }
}
