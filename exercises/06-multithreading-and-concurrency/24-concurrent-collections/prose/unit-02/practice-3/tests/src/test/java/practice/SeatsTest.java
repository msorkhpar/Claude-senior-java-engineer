package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SeatsTest {

    /**
     * A map whose containsKey() and get() line two worker threads up: each looks, then waits (at most 3 s)
     * until the other has also checked, so a check-then-act solution always has both threads see
     * the seat free before either writes. The map's own atomic methods never call these two.
     */
    private static final class SteppedMap extends ConcurrentHashMap<String, String> {
        private final CyclicBarrier bothChecked = new CyclicBarrier(2);
        private final Set<Thread> workers = ConcurrentHashMap.newKeySet();

        private void step() {
            if (workers.remove(Thread.currentThread())) {
                try {
                    bothChecked.await(3, TimeUnit.SECONDS);
                } catch (TimeoutException | BrokenBarrierException e) {
                    // the other thread never checked: go on alone
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public boolean containsKey(Object key) {
            boolean seen = super.containsKey(key);
            step();
            return seen;
        }

        @Override
        public String get(Object key) {
            String seen = super.get(key);
            step();
            return seen;
        }
    }

    private static String name(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void claimsAFreeSeat() {
        ConcurrentHashMap<String, String> seats = new ConcurrentHashMap<>();
        assertThat(Seats.claim(seats, name("A1"), name("ann"))).isEqualTo("ann");
        assertThat(Seats.claim(seats, name("B2"), name("bob"))).isEqualTo("bob");
        assertThat(seats).containsEntry("A1", "ann").containsEntry("B2", "bob").hasSize(2);
    }

    @Test
    void firstClaimKeepsTheSeat() {
        ConcurrentHashMap<String, String> seats = new ConcurrentHashMap<>();
        Seats.claim(seats, name("A1"), name("ann"));
        assertThat(Seats.claim(seats, name("A1"), name("bob"))).isEqualTo("ann");
        assertThat(seats).containsEntry("A1", "ann").hasSize(1);
    }

    @Test
    void simultaneousClaimsAgree() throws InterruptedException {
        SteppedMap seats = new SteppedMap();
        String[] people = {name("ann"), name("bob")};
        AtomicReferenceArray<String> answers = new AtomicReferenceArray<>(2);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread[] threads = new Thread[2];
        for (int i = 0; i < 2; i++) {
            int me = i;
            threads[i] = new Thread(() -> answers.set(me, Seats.claim(seats, name("A1"), people[me])));
            threads[i].setDaemon(true);
            threads[i].setUncaughtExceptionHandler((t, e) -> failure.set(e));
            seats.workers.add(threads[i]);
        }
        threads[0].start();
        threads[1].start();
        threads[0].join(8_000);
        threads[1].join(8_000);
        assertThat(threads[0].isAlive() || threads[1].isAlive()).as("both claims finished").isFalse();
        assertThat(failure.get()).isNull();
        assertThat(answers.get(0)).as("both claims name one winner").isEqualTo(answers.get(1));
        assertThat(seats).containsEntry("A1", answers.get(0)).hasSize(1);
    }
}
