package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import practice.ShippingQuotes.ByWeight;
import practice.ShippingQuotes.Parcel;
import practice.ShippingQuotes.RateTable;

class ShippingQuotesTest {

    static final Map<String, Long> RATES = Map.of("north", 300L, "south", 410L, "island", 520L);

    /** A rate table whose lookups for one zone wait until the test lets them go. */
    static final class HeldRateTable implements RateTable {
        final String heldZone;
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        HeldRateTable(String heldZone) {
            this.heldZone = heldZone;
        }

        @Override
        public long centsPerKg(String zone) {
            if (zone.equals(heldZone)) {
                entered.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return RATES.get(zone);
        }
    }

    static ExecutorService daemonPool() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @Test
    void quotesEachParcelByWeightAndZone() {
        ByWeight policy = new ByWeight(RATES::get);
        assertThat(policy.quoteCents(new Parcel("p-1", 250, "north"))).isEqualTo(300);
        assertThat(policy.quoteCents(new Parcel("p-2", 2_001, "south"))).isEqualTo(1_230);
        assertThat(policy.quoteCents(new Parcel("p-3", 4_000, "island"))).isEqualTo(2_496);
        assertThat(policy.quoteCents(new Parcel("p-4", 1_000, "north"))).isEqualTo(300);
    }

    @Test
    void aQuoteWaitingOnTheRateTableKeepsItsOwnParcel() throws Exception {
        HeldRateTable rates = new HeldRateTable("north");
        ByWeight policy = new ByWeight(rates);
        ExecutorService pool = daemonPool();
        try {
            Future<Long> slow = pool.submit(() -> policy.quoteCents(new Parcel("p-7", 3_500, "north")));
            assertThat(rates.entered.await(5, TimeUnit.SECONDS)).as("the north lookup started").isTrue();
            Future<Long> quick = pool.submit(() -> policy.quoteCents(new Parcel("p-8", 600, "island")));
            try {
                quick.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException waitingBehindTheSlowQuote) {
                // allowed here: this case checks the prices only
            }
            rates.release.countDown();
            assertThat(slow.get(5, TimeUnit.SECONDS)).as("p-7, 4 kg north").isEqualTo(1_200);
            assertThat(quick.get(5, TimeUnit.SECONDS)).as("p-8, 1 kg island").isEqualTo(624);
        } finally {
            rates.release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void aSlowRateLookupDoesNotHoldUpOtherQuotes() throws Exception {
        HeldRateTable rates = new HeldRateTable("south");
        ByWeight policy = new ByWeight(rates);
        ExecutorService pool = daemonPool();
        try {
            pool.submit(() -> policy.quoteCents(new Parcel("p-5", 900, "south")));
            assertThat(rates.entered.await(5, TimeUnit.SECONDS)).as("the south lookup started").isTrue();
            Future<Long> quick = pool.submit(() -> policy.quoteCents(new Parcel("p-6", 5_200, "north")));
            long cents;
            try {
                cents = quick.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new AssertionError("a north quote waited for a south rate lookup to finish");
            }
            assertThat(cents).isEqualTo(1_800);
        } finally {
            rates.release.countDown();
            pool.shutdownNow();
        }
    }
}
