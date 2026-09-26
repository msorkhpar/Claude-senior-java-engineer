package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LeasesTest {

    private static Thread leaseOn(Leases leases, int n, AtomicReference<Object> result) {
        Thread t = new Thread(() -> {
            try {
                leases.lease(n);
                result.set("leased");
            } catch (Exception e) {
                result.set(e);
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Waits (at most 3 s) until {@code t} is parked; returns whether it is. */
    private static boolean parked(Thread t) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (t.getState() != Thread.State.WAITING && t.isAlive() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        return t.getState() == Thread.State.WAITING;
    }

    @Test
    void leaseAndGiveBack() throws InterruptedException {
        Leases leases = new Leases(4);
        leases.lease(3);
        assertThat(leases.available()).isEqualTo(1);
        leases.giveBack(3);
        assertThat(leases.tryLease(2)).isTrue();
        assertThat(leases.available()).isEqualTo(2);
    }

    @Test
    void waitingLeaseHoldsNothing() throws InterruptedException {
        Leases leases = new Leases(4);
        leases.lease(2);
        AtomicReference<Object> result = new AtomicReference<>();
        Thread waiter = leaseOn(leases, 3, result);
        assertThat(parked(waiter)).as("lease(3) waits with only 2 free").isTrue();
        int whileWaiting = leases.available();
        leases.giveBack(2);
        waiter.join(3_000);
        assertThat(whileWaiting).as("the waiting lease holds none of the free buffers").isEqualTo(2);
        assertThat(result.get()).isEqualTo("leased");
        assertThat(leases.available()).isEqualTo(1);
    }

    @Test
    void failedTryLeaseTakesNothing() throws InterruptedException {
        Leases leases = new Leases(4);
        leases.lease(2);
        assertThat(leases.tryLease(3)).isFalse();
        assertThat(leases.available()).isEqualTo(2);
    }

    @Test
    void interruptedLeaseThrows() throws InterruptedException {
        Leases leases = new Leases(4);
        leases.lease(2);
        AtomicReference<Object> result = new AtomicReference<>();
        Thread waiter = leaseOn(leases, 3, result);
        assertThat(parked(waiter)).isTrue();
        waiter.interrupt();
        waiter.join(3_000);
        boolean left = !waiter.isAlive();
        int afterInterrupt = leases.available();
        leases.giveBack(2);
        waiter.join(3_000);
        assertThat(left).as("the interrupted lease stopped waiting").isTrue();
        assertThat(result.get()).isInstanceOf(InterruptedException.class);
        assertThat(afterInterrupt).as("the interrupted lease holds none of the buffers").isEqualTo(2);
    }
}
