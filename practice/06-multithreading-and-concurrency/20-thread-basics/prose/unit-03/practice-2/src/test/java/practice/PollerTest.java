package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PollerTest {

    /** Runs waitUntil on a daemon thread that never sees its condition hold, interrupts it while it sleeps. */
    private static boolean[] interruptWhileSleeping() throws InterruptedException {
        AtomicReference<Boolean> returned = new AtomicReference<>();
        AtomicBoolean flagAfter = new AtomicBoolean();
        Thread waiter = new Thread(() -> {
            returned.set(Poller.waitUntil(() -> false, 60_000));
            flagAfter.set(Thread.currentThread().isInterrupted());
        });
        waiter.setDaemon(true);
        waiter.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (waiter.getState() != Thread.State.TIMED_WAITING && waiter.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        waiter.interrupt();
        waiter.join(5_000);
        assertThat(waiter.isAlive()).as("the waiter gave way to the interrupt").isFalse();
        return new boolean[] {returned.get() != null && !returned.get(), flagAfter.get()};
    }

    @Test
    void returnsOnceTheConditionHolds() {
        AtomicInteger checks = new AtomicInteger();
        assertThat(Poller.waitUntil(() -> checks.incrementAndGet() == 3, 1)).isTrue();
        assertThat(checks.get()).isEqualTo(3);
        assertThat(Poller.waitUntil(() -> true, 60_000)).isTrue();
    }

    @Test
    void stopsWhenInterruptedWhileSleeping() throws InterruptedException {
        boolean[] seen = interruptWhileSleeping();
        assertThat(seen[0]).as("waitUntil returned false").isTrue();
    }

    @Test
    void restoresTheInterruptFlag() throws InterruptedException {
        boolean[] seen = interruptWhileSleeping();
        assertThat(seen[0]).as("waitUntil returned false").isTrue();
        assertThat(seen[1]).as("the caller's interrupt flag is set after the call").isTrue();
    }

    @Test
    void anAlreadyInterruptedCallerKeepsItsFlag() {
        Thread.currentThread().interrupt();
        boolean result;
        boolean flag;
        try {
            result = Poller.waitUntil(() -> false, 60_000);
        } finally {
            flag = Thread.interrupted();
        }
        assertThat(result).isFalse();
        assertThat(flag).as("the caller's interrupt flag is still set").isTrue();
    }
}
