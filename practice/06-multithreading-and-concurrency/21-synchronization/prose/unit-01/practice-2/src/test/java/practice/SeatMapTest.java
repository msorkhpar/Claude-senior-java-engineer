package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SeatMapTest {

    /** A gateway that records charges; it can hold the first charge open, decline cards, or throw. */
    static class Gateway implements SeatMap.Gateway {
        final List<String> charges = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch firstInside = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicBoolean first = new AtomicBoolean(true);
        final boolean holdFirst;
        final String declined;

        Gateway(boolean holdFirst, String declined) {
            this.holdFirst = holdFirst;
            this.declined = declined;
        }

        @Override
        public boolean charge(String card, long cents) {
            charges.add(card + " " + cents);
            if (card.startsWith("timeout")) throw new IllegalStateException("gateway timeout");
            if (holdFirst && first.getAndSet(false)) {
                firstInside.countDown();
                try {
                    release.await(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return !card.equals(declined);
        }
    }

    private final List<Thread> started = new ArrayList<>();

    Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        started.add(t);
        return t;
    }

    @AfterEach
    void finishThreads() throws InterruptedException {
        for (Thread t : started) t.join(10_000);
    }

    static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    static void awaitUntil(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.onSpinWait();
    }

    @Test
    void sellsAFreeSeatOnceAndChargesForIt() {
        Gateway gateway = new Gateway(false, null);
        SeatMap map = new SeatMap(List.of("C7", "C8"), 6_450, gateway);

        assertThat(map.book("C7", "card-4417")).isTrue();
        assertThat(map.book("C7", "card-9023")).isFalse();

        assertThat(map.state("C7")).isEqualTo(SeatMap.State.SOLD);
        assertThat(map.state("C8")).isEqualTo(SeatMap.State.FREE);
        assertThat(gateway.charges).containsExactly("card-4417 6450");
    }

    @Test
    void aDeclinedCardLeavesTheSeatForTheNextBuyer() {
        Gateway gateway = new Gateway(false, "card-0001");
        SeatMap map = new SeatMap(List.of("F12"), 3_900, gateway);

        assertThat(map.book("F12", "card-0001")).isFalse();
        assertThat(map.state("F12")).isEqualTo(SeatMap.State.FREE);
        assertThat(map.book("F12", "card-0002")).isTrue();
        assertThat(map.state("F12")).isEqualTo(SeatMap.State.SOLD);
    }

    @Test
    void aSeatBeingPaidForIsNotChargedToASecondBuyer() {
        Gateway gateway = new Gateway(true, null);
        SeatMap map = new SeatMap(List.of("K3"), 8_800, gateway);
        AtomicReference<Boolean> firstResult = new AtomicReference<>();
        AtomicReference<Boolean> secondResult = new AtomicReference<>();
        Thread first = daemon(() -> firstResult.set(map.book("K3", "card-1111")));
        try {
            assertThat(gateway.firstInside.await(10, TimeUnit.SECONDS)).isTrue();
            Thread second = daemon(() -> secondResult.set(map.book("K3", "card-2222")));
            awaitUntil(() -> !second.isAlive() || blockedBy(second, first));
            assertThat(gateway.charges).as("cards charged for seat K3").containsExactly("card-1111 8800");
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            gateway.release.countDown();
        }
    }

    @Test
    void anotherSeatSellsWhileACardIsBeingCharged() {
        Gateway gateway = new Gateway(true, null);
        SeatMap map = new SeatMap(List.of("A1", "A2"), 5_000, gateway);
        Thread first = daemon(() -> map.book("A1", "card-3333"));
        try {
            assertThat(gateway.firstInside.await(10, TimeUnit.SECONDS)).isTrue();
            AtomicReference<Boolean> result = new AtomicReference<>();
            Thread second = daemon(() -> result.set(map.book("A2", "card-4444")));
            awaitUntil(() -> !second.isAlive() || blockedBy(second, first));
            assertThat(second.isAlive()).as("booking A2 waited for the charge for A1").isFalse();
            assertThat(result.get()).isTrue();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            gateway.release.countDown();
        }
    }

    @Test
    void aGatewayTimeoutLeavesTheSeatSellable() {
        Gateway gateway = new Gateway(false, null);
        SeatMap map = new SeatMap(List.of("D4"), 4_200, gateway);

        assertThatThrownBy(() -> map.book("D4", "timeout-card")).hasMessage("gateway timeout");

        assertThat(map.state("D4")).isEqualTo(SeatMap.State.FREE);
        assertThat(map.book("D4", "card-5555")).isTrue();
    }
}
