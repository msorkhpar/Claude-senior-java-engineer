package practice;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class SeatMapTest {

    /** Sends the hold request from a daemon thread of its own; the future completes with its answer. */
    private static CompletableFuture<SeatMap.Answer> holdFromAnotherThread(SeatMap map, String seat, String customer,
                                                                          Thread[] thread) {
        CompletableFuture<SeatMap.Answer> answer = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                answer.complete(map.hold(seat, customer));
            } catch (Throwable e) {
                answer.completeExceptionally(e);
            }
        }, "request-" + customer);
        t.setDaemon(true);
        thread[0] = t;
        t.start();
        return answer;
    }

    private static SeatMap.Answer within5s(CompletableFuture<SeatMap.Answer> answer) throws Exception {
        return answer.get(5, TimeUnit.SECONDS);
    }

    @Test
    void aFreeSeatIsHeldAndASecondCustomerIsTurnedAway() throws Exception {
        SeatMap map = new SeatMap(Duration.ofSeconds(2));

        assertThat(map.hold("C-14", "cust-311")).isEqualTo(SeatMap.Answer.HELD);
        assertThat(map.hold("C-14", "cust-902")).isEqualTo(SeatMap.Answer.TAKEN);
        assertThat(map.hold("C-15", "cust-902")).isEqualTo(SeatMap.Answer.HELD);
        assertThat(map.holder("C-14")).isEqualTo("cust-311");
    }

    @Test
    void aRequestAnsweredBusyLeavesTheSeatFree() throws Exception {
        SeatMap map = new SeatMap(Duration.ofMillis(40));
        Thread[] request = new Thread[1];
        CompletableFuture<SeatMap.Answer> answer;
        map.lock.lock();                                   // the re-sync holds the map throughout
        try {
            answer = holdFromAnotherThread(map, "F-7", "cust-517", request);
            assertThat(within5s(answer)).isEqualTo(SeatMap.Answer.BUSY);
        } finally {
            map.lock.unlock();
        }
        assertThat(map.holder("F-7")).isNull();
    }

    @Test
    void aReSyncThatEndsWithinTheBudgetDoesNotTurnTheRequestAway() throws Exception {
        SeatMap map = new SeatMap(Duration.ofSeconds(30));
        Thread[] request = new Thread[1];
        CompletableFuture<SeatMap.Answer> answer;
        map.lock.lock();                                   // the re-sync holds the map...
        try {
            answer = holdFromAnotherThread(map, "A-2", "cust-640", request);
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!map.lock.hasQueuedThread(request[0]) && !answer.isDone() && System.nanoTime() < end) {
                Thread.onSpinWait();
            }
        } finally {
            map.lock.unlock();                             // ...and ends while the request waits
        }
        assertThat(within5s(answer)).isEqualTo(SeatMap.Answer.HELD);
        assertThat(map.holder("A-2")).isEqualTo("cust-640");
    }

    @Test
    void aReSyncThatNeverEndsStillGetsTheCustomerAnAnswer() throws Exception {
        SeatMap map = new SeatMap(Duration.ofMillis(40));
        Thread[] request = new Thread[1];
        map.lock.lock();
        try {
            CompletableFuture<SeatMap.Answer> answer = holdFromAnotherThread(map, "K-30", "cust-118", request);
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!answer.isDone() && System.nanoTime() < end) Thread.onSpinWait();
            assertThat(answer).as("the customer has an answer").isCompletedWithValue(SeatMap.Answer.BUSY);
        } finally {
            map.lock.unlock();
        }
    }
}
