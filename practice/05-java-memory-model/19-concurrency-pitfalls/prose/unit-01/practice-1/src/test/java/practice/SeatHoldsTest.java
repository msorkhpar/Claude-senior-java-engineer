package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SeatHoldsTest {

    @Test
    void holdsGoToTheFirstCustomerWhoAsks() throws Exception {
        ConcurrentHashMap<String, String> flight = new ConcurrentHashMap<>();
        assertThat(call(() -> new SeatHolds(flight).reserve("14A", "noor"))).isTrue();
        assertThat(call(() -> new SeatHolds(flight).reserve("14B", "ivan"))).isTrue();
        assertThat(new SeatHolds(flight).holderOf("14A")).contains("noor");
        assertThat(new SeatHolds(flight).holderOf("14B")).contains("ivan");
        assertThat(call(() -> new SeatHolds(flight).reserve("14A", "ivan"))).isFalse();
        assertThat(new SeatHolds(flight).holderOf("19K")).isEmpty();
    }

    @Test
    void aReleaseFreesOnlyTheCustomersOwnHold() throws Exception {
        ConcurrentHashMap<String, String> flight = new ConcurrentHashMap<>();
        assertThat(call(() -> new SeatHolds(flight).reserve("22C", "mei"))).isTrue();
        assertThat(call(() -> new SeatHolds(flight).release("22C", "olu"))).isFalse();
        assertThat(new SeatHolds(flight).holderOf("22C")).contains("mei");
        assertThat(call(() -> new SeatHolds(flight).release("22C", "mei"))).isTrue();
        assertThat(new SeatHolds(flight).holderOf("22C")).isEmpty();
        assertThat(call(() -> new SeatHolds(flight).reserve("22C", "olu"))).isTrue();
        assertThat(new SeatHolds(flight).holderOf("22C")).contains("olu");
    }

    @Test
    void twoRequestsForOneFreeSeatGiveItToOneCustomer() throws Exception {
        PausingMap flight = new PausingMap();
        FutureTask<Boolean> kai = flight.pausedAfterItsFirstRead(() -> new SeatHolds(flight).reserve("31F", "kai"));
        FutureTask<Boolean> lena = flight.runWhileThePausedOneWaits(() -> new SeatHolds(flight).reserve("31F", "lena"));

        boolean kaiGotIt = kai.get(10, TimeUnit.SECONDS);
        boolean lenaGotIt = lena.get(10, TimeUnit.SECONDS);
        assertThat(kaiGotIt ^ lenaGotIt)
                .as("exactly one of the two requests was told yes (kai: " + kaiGotIt + ", lena: " + lenaGotIt + ")")
                .isTrue();
        assertThat(new SeatHolds(flight).holderOf("31F")).contains(kaiGotIt ? "kai" : "lena");
    }

    @Test
    void aRefusedRequestLeavesTheHolderInPlace() throws Exception {
        ConcurrentHashMap<String, String> flight = new ConcurrentHashMap<>();
        assertThat(call(() -> new SeatHolds(flight).reserve("8D", "ravi"))).isTrue();
        assertThat(call(() -> new SeatHolds(flight).reserve("8D", "sol"))).isFalse();
        assertThat(new SeatHolds(flight).holderOf("8D")).as("the seat after sol was refused").contains("ravi");
    }

    @Test
    void aLateReleaseLeavesTheNextCustomersHoldInPlace() throws Exception {
        PausingMap flight = new PausingMap();
        flight.put("5B", "ana");
        // ana's first click on "release" is paused right after it reads the map. Meanwhile her
        // second click releases the seat, and tomas takes it.
        FutureTask<Boolean> firstClick = flight.pausedAfterItsFirstRead(() -> new SeatHolds(flight).release("5B", "ana"));
        FutureTask<Boolean> tomas = flight.runWhileThePausedOneWaits(() -> {
            new SeatHolds(flight).release("5B", "ana");
            return new SeatHolds(flight).reserve("5B", "tomas");
        });

        firstClick.get(10, TimeUnit.SECONDS);
        assertThat(tomas.get(10, TimeUnit.SECONDS)).as("tomas was told he has 5B").isTrue();
        assertThat(new SeatHolds(flight).holderOf("5B")).as("the holder of 5B after both of ana's clicks").contains("tomas");
    }

    /** Runs the call on its own daemon thread and waits for it, so a call that never returns fails the test. */
    private static <T> T call(Callable<T> body) throws Exception {
        return start(body).get(10, TimeUnit.SECONDS);
    }

    private static <T> FutureTask<T> start(Callable<T> body) {
        FutureTask<T> task = new FutureTask<>(body);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return task;
    }

    /**
     * A flight's map that can pause one chosen thread right after its first read (get, containsKey
     * or getOrDefault), until a second request has had its turn. Writes are never paused.
     */
    static final class PausingMap extends ConcurrentHashMap<String, String> {
        private final AtomicReference<Thread> pauseWho = new AtomicReference<>();
        private final CountDownLatch pausedOrDone = new CountDownLatch(1);
        private final CountDownLatch goOn = new CountDownLatch(1);

        /** Starts the call on a thread that will be paused after its first read of this map. */
        <T> FutureTask<T> pausedAfterItsFirstRead(Callable<T> body) throws InterruptedException {
            FutureTask<T> task = new FutureTask<>(() -> {
                try {
                    return body.call();
                } finally {
                    pausedOrDone.countDown();
                }
            });
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            pauseWho.set(thread);
            thread.start();
            assertThat(pausedOrDone.await(10, TimeUnit.SECONDS)).as("the first request read the map or finished").isTrue();
            return task;
        }

        /**
         * Runs a second call while the first is paused, then lets the first go on. A second call
         * that waits for the first one (for a lock it holds) is let through after a bounded wait.
         */
        <T> FutureTask<T> runWhileThePausedOneWaits(Callable<T> body) throws Exception {
            FutureTask<T> task = start(body);
            try {
                task.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException waitsForTheFirst) {
                // it is waiting for the paused request; that is allowed
            } finally {
                pauseWho.set(null);
                goOn.countDown();
            }
            return task;
        }

        private void pauseIfChosen() {
            if (pauseWho.compareAndSet(Thread.currentThread(), null)) {
                pausedOrDone.countDown();
                try {
                    goOn.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public String get(Object key) {
            String value = super.get(key);
            pauseIfChosen();
            return value;
        }

        @Override
        public boolean containsKey(Object key) {
            boolean present = super.containsKey(key);
            pauseIfChosen();
            return present;
        }

        @Override
        public String getOrDefault(Object key, String defaultValue) {
            String value = super.getOrDefault(key, defaultValue);
            pauseIfChosen();
            return value;
        }
    }
}
