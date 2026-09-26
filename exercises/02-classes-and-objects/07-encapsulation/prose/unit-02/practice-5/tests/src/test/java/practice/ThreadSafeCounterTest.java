package practice;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadSafeCounterTest {

    @Test
    void countsIncrements() {
        AtomicInteger steps = new AtomicInteger();
        ThreadSafeCounter counter = new ThreadSafeCounter(steps::incrementAndGet);
        assertThat(counter.getCount()).isZero();
        counter.incrementCount();
        counter.incrementCount();
        counter.incrementCount();
        assertThat(counter.getCount()).isEqualTo(3);
        assertThat(steps.get()).isEqualTo(3);
        assertThat(new ThreadSafeCounter().getCount()).isZero();
    }

    @Test
    void noIncrementIsLostAcrossThreads() throws InterruptedException {
        // Each increment waits (up to two seconds) in its step for the other thread to reach
        // its own step: an increment that lets both threads read before either writes loses one.
        CyclicBarrier bothRead = new CyclicBarrier(2);
        ThreadSafeCounter counter = new ThreadSafeCounter(() -> {
            try {
                bothRead.await(2, TimeUnit.SECONDS);
            } catch (TimeoutException | BrokenBarrierException e) {
                // the other thread could not arrive meanwhile: the increments did not overlap
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread first = new Thread(counter::incrementCount);
        Thread second = new Thread(counter::incrementCount);
        first.start();
        second.start();
        first.join();
        second.join();
        assertThat(counter.getCount()).isEqualTo(2);
    }
}
