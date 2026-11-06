package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class QuoteRoundTest {

    private static final List<String> SUPPLIERS = List.of("northwind", "acme-parts", "brightline");

    /** Collects the recorded prices, and counts each one down on a latch the test can wait on. */
    static final class Recorded implements QuoteRound.Quotes {
        final Map<String, Integer> prices = new ConcurrentHashMap<>();
        final CountDownLatch all;

        Recorded(int expected) {
            all = new CountDownLatch(expected);
        }

        @Override
        public void record(String supplier, int cents) {
            prices.put(supplier, cents);
            all.countDown();
        }
    }

    /** Runs askAll on a daemon thread and keeps a copy of what had been recorded when it returned. */
    static final class Caller {
        final AtomicReference<Map<String, Integer>> atReturn = new AtomicReference<>();
        final CountDownLatch returned = new CountDownLatch(1);

        Caller(QuoteRound round, Recorded recorded) {
            Thread t = new Thread(() -> {
                try {
                    round.askAll(SUPPLIERS, recorded);
                    atReturn.set(Map.copyOf(recorded.prices));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    returned.countDown();
                }
            }, "page-request");
            t.setDaemon(true);
            t.start();
        }
    }

    private static int await(CountDownLatch gate) {
        try {
            gate.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 0;
    }

    @Test
    void recordsEverySuppliersPrice() throws InterruptedException {
        QuoteRound round = new QuoteRound(supplier -> supplier.length() * 125);
        Recorded recorded = new Recorded(SUPPLIERS.size());

        round.askAll(SUPPLIERS, recorded);

        assertThat(recorded.all.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(recorded.prices).containsExactlyInAnyOrderEntriesOf(
                Map.of("northwind", 1125, "acme-parts", 1250, "brightline", 1250));
    }

    @Test
    void asksEverySupplierAtTheSameTime() throws InterruptedException {
        CountDownLatch asked = new CountDownLatch(SUPPLIERS.size());
        QuoteRound round = new QuoteRound(supplier -> {
            asked.countDown();
            try {
                // A supplier answers only while the others are being asked as well.
                return asked.await(2, TimeUnit.SECONDS) ? 990 : -1;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        });
        Recorded recorded = new Recorded(SUPPLIERS.size());

        round.askAll(SUPPLIERS, recorded);

        assertThat(recorded.all.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(recorded.prices.values()).as("prices of suppliers that were asked alone are -1")
                .containsOnly(990);
    }

    @Test
    void returnsOnlyOnceEveryPriceIsIn() throws InterruptedException {
        CountDownLatch answer = new CountDownLatch(1);
        QuoteRound round = new QuoteRound(supplier -> await(answer) + 700);
        Recorded recorded = new Recorded(SUPPLIERS.size());

        Caller caller = new Caller(round, recorded);
        try {
            caller.returned.await(1, TimeUnit.SECONDS);
        } finally {
            answer.countDown();
        }

        assertThat(caller.returned.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(caller.atReturn.get()).as("prices recorded when askAll returned").hasSize(3);
    }

    @Test
    void waitsForTheFirstSupplierWhenItAnswersLast() throws InterruptedException {
        CountDownLatch slowAnswer = new CountDownLatch(1);
        QuoteRound round = new QuoteRound(supplier ->
                supplier.equals("northwind") ? await(slowAnswer) + 1480 : 1510);
        Recorded recorded = new Recorded(SUPPLIERS.size());

        Caller caller = new Caller(round, recorded);
        try {
            caller.returned.await(1, TimeUnit.SECONDS);
        } finally {
            slowAnswer.countDown();
        }

        assertThat(caller.returned.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(caller.atReturn.get()).as("prices recorded when askAll returned")
                .containsEntry("northwind", 1480);
    }
}
