package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PoolTest {

    interface Call {
        Object call() throws Exception;
    }

    private static Thread start(AtomicReference<Object> result, Call body) {
        Thread t = new Thread(() -> {
            try {
                result.set(body.call());
            } catch (Exception e) {
                result.set(e);
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** A resource name built at run time, so no two resources share an interned literal. */
    private static String resource(int n) {
        return new StringBuilder("db-").append(n).toString();
    }

    @Test
    void leasesAndReturns() throws InterruptedException {
        Pool<String> pool = new Pool<>(List.of(resource(1), resource(2)));
        assertThat(pool.available()).isEqualTo(2);
        String a = pool.acquire();
        String b = pool.acquire();
        assertThat(List.of(a, b)).containsExactlyInAnyOrder("db-1", "db-2");
        assertThat(pool.available()).isZero();
        pool.release(a);
        pool.release(b);
        assertThat(pool.available()).isEqualTo(2);
    }

    @Test
    void acquireWaitsWhenExhausted() throws InterruptedException {
        Pool<String> pool = new Pool<>(List.of(resource(1)));
        String only = pool.acquire();
        AtomicReference<Object> got = new AtomicReference<>();
        Thread waiter = start(got, pool::acquire);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (waiter.getState() != Thread.State.WAITING && waiter.isAlive() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(waiter.getState()).as("acquire waits on an empty pool").isEqualTo(Thread.State.WAITING);
        pool.release(only);
        waiter.join(3_000);
        assertThat(got.get()).as("the waiter got the released object itself").isSameAs(only);
        assertThat(pool.available()).isZero();
    }

    @Test
    void timedAcquireGivesUp() throws InterruptedException {
        Pool<String> pool = new Pool<>(List.of(resource(1)));
        String only = pool.acquire();
        AtomicReference<Object> got = new AtomicReference<>("unset");
        Thread waiter = start(got, () -> pool.tryAcquire(100_000, TimeUnit.MICROSECONDS));
        waiter.join(4_000);
        boolean gaveUp = !waiter.isAlive();
        pool.release(only);
        waiter.join(3_000);
        assertThat(gaveUp).as("the timed acquire gave up while the pool was empty").isTrue();
        assertThat(got.get()).isNull();
    }

    @Test
    void timedAcquireWaitsForRelease() throws InterruptedException {
        Pool<String> pool = new Pool<>(List.of(resource(1)));
        String only = pool.acquire();
        AtomicReference<Object> got = new AtomicReference<>("unset");
        Thread waiter = start(got, () -> pool.tryAcquire(8, TimeUnit.SECONDS));
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (waiter.getState() != Thread.State.TIMED_WAITING && waiter.isAlive() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        boolean waiting = waiter.getState() == Thread.State.TIMED_WAITING;
        pool.release(only);
        waiter.join(3_000);
        assertThat(waiting).as("the timed acquire waits on an empty pool").isTrue();
        assertThat(got.get()).isSameAs(only);
    }

    @Test
    void doubleReleaseIsRefused() throws InterruptedException {
        Pool<String> pool = new Pool<>(List.of(resource(1)));
        String r = pool.acquire();
        pool.release(r);
        assertThatThrownBy(() -> pool.release(r)).isInstanceOf(IllegalArgumentException.class);
        assertThat(pool.available()).as("no permit inflation").isEqualTo(1);
    }

    @Test
    void equalStrangerIsRefused() throws InterruptedException {
        Pool<String> pool = new Pool<>(List.of(resource(1)));
        String leased = pool.acquire();
        String stranger = resource(1);
        assertThat(stranger).isEqualTo(leased).isNotSameAs(leased);
        assertThatThrownBy(() -> pool.release(stranger)).isInstanceOf(IllegalArgumentException.class);
        assertThat(pool.available()).isZero();
        pool.release(leased);
        assertThat(pool.available()).isEqualTo(1);
    }

    @Test
    void theTimeoutIsKeptExactly() throws Exception {
        Pool<String> pool = new Pool<>(List.of(resource(1)));
        String only = pool.acquire();
        long start = System.nanoTime();
        String got = pool.tryAcquire(250_000, TimeUnit.MICROSECONDS);
        long waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(got).isNull();
        assertThat(waitedMs).as("a timeout of 250,000 microseconds waits the full 250 ms").isGreaterThanOrEqualTo(245);
        start = System.nanoTime();
        got = pool.tryAcquire(1_600, TimeUnit.MILLISECONDS);
        waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(got).isNull();
        assertThat(waitedMs).as("a timeout of 1,600 ms waits the full 1.6 s, not a rounded second").isGreaterThanOrEqualTo(1_595);
        pool.release(only);
    }
}
