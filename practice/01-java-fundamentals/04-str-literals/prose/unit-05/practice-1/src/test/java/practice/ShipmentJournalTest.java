package practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentJournalTest {

    private static final ExecutorService POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "scanner");
        t.setDaemon(true);
        return t;
    });

    @AfterAll
    static void stopPool() {
        POOL.shutdownNow();
    }

    private record Fixed(String parcel, String depot, int grams) implements ShipmentJournal.Event {
    }

    /** An event whose depot() lookup blocks until the test releases it. */
    private static final class SlowLookup implements ShipmentJournal.Event {
        final CountDownLatch inside = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        @Override
        public String parcel() {
            return "NL-5531";
        }

        @Override
        public String depot() {
            inside.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "OSL-4";
        }

        @Override
        public int grams() {
            return 2045;
        }
    }

    private static final String SLOW_LINE = "parcel=NL-5531 depot=OSL-4 grams=2045";
    private static final String FAST_LINE = "parcel=BE-0907 depot=ANR-2 grams=610";

    private record Outcome(List<String> lines, boolean fastFinishedWhileSlowWaited) {
    }

    /** One thread records an event whose depot() is slow; meanwhile another thread records a quick one. */
    private static Outcome slowAndFastTogether() throws Exception {
        ShipmentJournal journal = new ShipmentJournal();
        SlowLookup slow = new SlowLookup();
        Future<?> slowRecord = POOL.submit(() -> journal.record(slow));
        try {
            assertThat(slow.inside.await(5, TimeUnit.SECONDS)).as("the slow event reached depot()").isTrue();
            Future<?> fastRecord = POOL.submit(() -> journal.record(new Fixed("BE-0907", "ANR-2", 610)));
            boolean fastDone;
            try {
                fastRecord.get(3, TimeUnit.SECONDS);
                fastDone = true;
            } catch (TimeoutException stillWaiting) {
                fastDone = false;
            }
            slow.release.countDown();
            fastRecord.get(15, TimeUnit.SECONDS);
            slowRecord.get(15, TimeUnit.SECONDS);
            return new Outcome(journal.contents().lines().toList(), fastDone);
        } finally {
            slow.release.countDown();
        }
    }

    @Test
    void recordsOneLinePerEventInOrder() throws Exception {
        ShipmentJournal journal = new ShipmentJournal();
        Future<?> run = POOL.submit(() -> {
            journal.record(new Fixed("DE-1200", "HAM-1", 1500));
            journal.record(new Fixed("FR-0042", "LYS-9", 75));
            journal.record(new Fixed("IT-7781", "MIL-3", 30000));
        });
        run.get(10, TimeUnit.SECONDS);

        assertThat(journal.contents()).isEqualTo("""
                parcel=DE-1200 depot=HAM-1 grams=1500
                parcel=FR-0042 depot=LYS-9 grams=75
                parcel=IT-7781 depot=MIL-3 grams=30000
                """);
    }

    @Test
    void anotherThreadsLineArrivesWholeWhileOneEventIsSlow() throws Exception {
        Outcome outcome = slowAndFastTogether();

        assertThat(outcome.lines()).contains(FAST_LINE);
    }

    @Test
    void aSlowEventDoesNotHoldUpTheOtherThreads() throws Exception {
        Outcome outcome = slowAndFastTogether();

        assertThat(outcome.fastFinishedWhileSlowWaited())
                .as("the quick event was recorded while the slow one was still in depot()").isTrue();
    }

    @Test
    void theSlowEventsOwnLineComesOutWhole() throws Exception {
        Outcome outcome = slowAndFastTogether();

        assertThat(outcome.lines()).containsExactlyInAnyOrder(SLOW_LINE, FAST_LINE);
    }
}
