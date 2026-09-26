package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class RendezvousTest {

    private static Thread start(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static Thread awaitOn(Rendezvous rendezvous, long timeout, TimeUnit unit, AtomicReference<Object> result) {
        return start(() -> {
            try {
                result.set(rendezvous.awaitAll(timeout, unit));
            } catch (Exception e) {
                result.set(e);
            }
        });
    }

    private static void readyOnOwnThread(Rendezvous rendezvous) throws InterruptedException {
        Thread worker = start(rendezvous::ready);
        worker.join(3_000);
    }

    @Test
    void allReadyThenAwait() throws InterruptedException {
        Rendezvous rendezvous = new Rendezvous(3);
        for (int i = 0; i < 3; i++) {
            readyOnOwnThread(rendezvous);
        }
        assertThat(rendezvous.awaitAll(1, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void waitsForEveryWorker() throws InterruptedException {
        Rendezvous rendezvous = new Rendezvous(3);
        rendezvous.ready();
        rendezvous.ready();
        assertThat(rendezvous.awaitAll(100, TimeUnit.MILLISECONDS)).as("one worker is still missing").isFalse();
        rendezvous.ready();
        assertThat(rendezvous.awaitAll(3, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void givesUpAtTimeout() throws InterruptedException {
        Rendezvous rendezvous = new Rendezvous(2);
        AtomicReference<Object> result = new AtomicReference<>("unset");
        Thread coordinator = awaitOn(rendezvous, 100_000, TimeUnit.MICROSECONDS, result);
        coordinator.join(4_000);
        boolean gaveUp = !coordinator.isAlive();
        rendezvous.ready();
        rendezvous.ready();
        coordinator.join(3_000);
        assertThat(gaveUp).as("awaitAll gave up at its timeout").isTrue();
        assertThat(result.get()).isEqualTo(false);
    }

    @Test
    void waiterIsWokenByLateReadies() throws InterruptedException {
        Rendezvous rendezvous = new Rendezvous(2);
        AtomicReference<Object> result = new AtomicReference<>("unset");
        Thread coordinator = awaitOn(rendezvous, 8, TimeUnit.SECONDS, result);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (coordinator.getState() != Thread.State.TIMED_WAITING && coordinator.isAlive()
                && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        boolean waiting = coordinator.getState() == Thread.State.TIMED_WAITING;
        readyOnOwnThread(rendezvous);
        readyOnOwnThread(rendezvous);
        coordinator.join(3_000);
        assertThat(waiting).as("the coordinator waits for the workers").isTrue();
        assertThat(result.get()).isEqualTo(true);
    }

    @Test
    void theTimeoutIsKeptExactly() throws Exception {
        Rendezvous rendezvous = new Rendezvous(2);
        long start = System.nanoTime();
        boolean all = rendezvous.awaitAll(250_000, TimeUnit.MICROSECONDS);
        long waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(all).isFalse();
        assertThat(waitedMs).as("a timeout of 250,000 microseconds waits the full 250 ms").isGreaterThanOrEqualTo(245);
        start = System.nanoTime();
        all = rendezvous.awaitAll(1_600, TimeUnit.MILLISECONDS);
        waitedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(all).isFalse();
        assertThat(waitedMs).as("a timeout of 1,600 ms waits the full 1.6 s, not a rounded second").isGreaterThanOrEqualTo(1_595);
    }
}
