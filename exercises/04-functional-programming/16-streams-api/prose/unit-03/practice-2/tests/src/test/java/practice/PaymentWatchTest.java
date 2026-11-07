package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentWatchTest {

    private static final long FROM = 500_000;          // 5,000.00

    /** Feeds stop with this once a pipeline reads further than any review queue should. */
    private static final long FEED_GUARD = 200_000;

    private final AtomicInteger assessed = new AtomicInteger();

    /** Scores come from a table; unknown payments score 10. Counts every call. */
    private PaymentWatch watch(Map<String, Integer> scores) {
        return new PaymentWatch(p -> {
            assessed.incrementAndGet();
            return new PaymentWatch.Assessment(p, scores.getOrDefault(p.id(), 10));
        }, FROM);
    }

    private static PaymentWatch.Payment pay(String id, long cents) {
        return new PaymentWatch.Payment(id, "acct-" + id, cents);
    }

    /** Runs the reader's code on a daemon thread, so a pipeline that never ends fails instead of hanging. */
    private static <T> T within(Callable<T> call) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            Future<T> f = pool.submit(call);
            return f.get(20, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void largePaymentsAreQueuedHighestScoreFirst() throws Exception {
        PaymentWatch watch = watch(Map.of("P-31", 40, "P-32", 95, "P-33", 70));
        Stream<PaymentWatch.Payment> feed = Stream.of(
                pay("P-31", 720_000), pay("P-32", 1_250_000), pay("P-33", 500_000));

        String sheet = within(() -> watch.reviewSheet(feed, 3));

        assertThat(sheet).isEqualTo("P-32 95\nP-33 70\nP-31 40");
    }

    @Test
    void aFeedThatNeverEndsStillFillsTheQueue() throws Exception {
        AtomicLong read = new AtomicLong();
        Stream<PaymentWatch.Payment> live = Stream.generate(() -> {
            long n = read.incrementAndGet();
            if (n > FEED_GUARD) throw new IllegalStateException("the feed was read past " + FEED_GUARD + " payments");
            return pay("L-" + n, n % 4 == 0 ? 900_000 : 12_000);   // every 4th payment is large
        });
        PaymentWatch watch = watch(Map.of("L-4", 30, "L-8", 80));

        List<PaymentWatch.Assessment> queue = within(() -> watch.nextForReview(live, 2));

        assertThat(queue).extracting(a -> a.payment().id()).containsExactly("L-8", "L-4");
    }

    @Test
    void smallPaymentsDoNotTakeQueueSlots() throws Exception {
        PaymentWatch watch = watch(Map.of("Q-2", 55, "Q-4", 65, "Q-5", 99));
        Stream<PaymentWatch.Payment> feed = Stream.of(
                pay("Q-1", 4_999), pay("Q-2", 640_000), pay("Q-3", 8_100), pay("Q-4", 2_000_000),
                pay("Q-5", 3_100_000));

        List<PaymentWatch.Assessment> queue = within(() -> watch.nextForReview(feed, 2));

        assertThat(queue).extracting(a -> a.payment().id()).containsExactly("Q-4", "Q-2");
    }

    @Test
    void onlyQueuedPaymentsAreAssessed() throws Exception {
        PaymentWatch watch = watch(Map.of());
        Stream<PaymentWatch.Payment> feed = Stream.of(
                pay("R-1", 20_000), pay("R-2", 610_000), pay("R-3", 3_500), pay("R-4", 47_000),
                pay("R-5", 880_000), pay("R-6", 99_000), pay("R-7", 505_000), pay("R-8", 1_900_000));

        List<PaymentWatch.Assessment> queue = within(() -> watch.nextForReview(feed, 3));

        assertThat(queue).hasSize(3);
        assertThat(assessed.get()).isEqualTo(3);
    }
}
