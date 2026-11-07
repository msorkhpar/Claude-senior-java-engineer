package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OnceTest {

    record RateCard(String base, int version) {}

    private final AtomicInteger loads = new AtomicInteger();
    private final ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopThePool() {
        pool.shutdownNow();
    }

    /** Every get() goes through the pool with a timeout, so a get() that never returns fails the case. */
    private <T> T callFromPool(Supplier<T> s) throws Exception {
        return pool.submit(s::get).get(5, TimeUnit.SECONDS);
    }

    @Test
    void everyCallGetsTheCardLoadedByTheFirst() throws Exception {
        Once<RateCard> card = new Once<>(() -> new RateCard("CHF", loads.incrementAndGet()));

        RateCard first = callFromPool(card);
        RateCard second = callFromPool(card);
        RateCard third = callFromPool(card);

        assertThat(first).isEqualTo(new RateCard("CHF", 1));
        assertThat(second).isSameAs(first);
        assertThat(third).isSameAs(first);
        assertThat(loads).hasValue(1);
    }

    @Test
    void creatingItLoadsNothing() throws Exception {
        Once<RateCard> card = new Once<>(() -> new RateCard("SEK", loads.incrementAndGet()));

        assertThat(loads).as("loads before anyone asked").hasValue(0);
        assertThat(callFromPool(card)).isEqualTo(new RateCard("SEK", 1));
    }

    @Test
    void aSecondCallerDuringASlowLoadWaitsForThatLoad() throws Exception {
        CountDownLatch firstInside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Once<RateCard> card = new Once<>(() -> {
            int n = loads.incrementAndGet();
            if (n == 1) {
                firstInside.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);       // the upstream is slow
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return new RateCard("NOK", n);
        });

        Future<RateCard> first = pool.submit(card::get);
        assertThat(firstInside.await(5, TimeUnit.SECONDS)).as("first load started").isTrue();
        AtomicReference<Thread> secondThread = new AtomicReference<>();
        Future<RateCard> second = pool.submit(() -> {
            secondThread.set(Thread.currentThread());
            return card.get();
        });
        // Wait until the second caller is either held up or has started a load of its own.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (loads.get() < 2 && !second.isDone() && System.nanoTime() < deadline) {
            Thread t = secondThread.get();
            if (t != null) {
                Thread.State s = t.getState();
                if (s == Thread.State.BLOCKED || s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING) break;
            }
            Thread.onSpinWait();
        }
        release.countDown();

        RateCard a = first.get(5, TimeUnit.SECONDS);
        RateCard b = second.get(5, TimeUnit.SECONDS);
        assertThat(loads).as("loads").hasValue(1);
        assertThat(b).isSameAs(a);
    }

    @Test
    void aSourceThatAnswersNullIsAskedOnce() throws Exception {
        Once<RateCard> card = new Once<>(() -> {
            loads.incrementAndGet();
            return null;                                     // no card configured for this desk
        });

        callFromPool(card);
        callFromPool(card);
        assertThat(callFromPool(card)).isNull();
        assertThat(loads).hasValue(1);
    }

    @Test
    void aFailedLoadIsTriedAgainOnTheNextCall() throws Exception {
        UncheckedIOException outage = new UncheckedIOException(new IOException("upstream 503"));
        Once<RateCard> card = new Once<>(() -> {
            if (loads.incrementAndGet() == 1) throw outage;
            return new RateCard("DKK", loads.get());
        });

        assertThatThrownBy(() -> callFromPool(card)).hasCauseReference(outage);
        assertThat(callFromPool(card)).isEqualTo(new RateCard("DKK", 2));
        assertThat(callFromPool(card)).isEqualTo(new RateCard("DKK", 2));
        assertThat(loads).hasValue(2);
    }
}
