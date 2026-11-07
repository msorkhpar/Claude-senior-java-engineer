package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class WalletsTest {

    private static final Map<String, Long> OPENING = Map.of("ana", 50L, "ben", 20L, "cy", 0L);

    /** Readers run here, so a read that blocks fails its test instead of hanging it. */
    private final ExecutorService reader = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "test-reader");
        t.setDaemon(true);
        return t;
    });

    /** Holds the first transfer between its debit and its credit until released. */
    private final CountDownLatch paused = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private final Wallets.Seam pauseFirst = () -> {
        if (paused.getCount() == 0) return;
        paused.countDown();
        try {
            release.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    };

    @AfterEach
    void cleanUp() {
        release.countDown();
        reader.shutdownNow();
    }

    private Map<String, Long> read(Wallets wallets) throws Exception {
        Future<Map<String, Long>> read = reader.submit(wallets::balances);
        return read.get(2, TimeUnit.SECONDS);
    }

    @Test
    void everyTransferShowsInTheBalances() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, () -> { })) {
            assertThat(wallets.transfer("ana", "cy", 15).get(5, TimeUnit.SECONDS)).isTrue();
            assertThat(wallets.transfer("ben", "ana", 5).get(5, TimeUnit.SECONDS)).isTrue();
            assertThat(wallets.transfer("cy", "ben", 100).get(5, TimeUnit.SECONDS)).isFalse();
            assertThat(wallets.transfer("cy", "ben", 10).get(5, TimeUnit.SECONDS)).isTrue();

            assertThat(read(wallets)).isEqualTo(Map.of("ana", 40L, "ben", 25L, "cy", 5L));
        }
    }

    @Test
    void aReadDuringATransferShowsTheBalancesFromBeforeIt() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, pauseFirst)) {
            Future<Boolean> moving = wallets.transfer("ana", "cy", 15);
            assertThat(paused.await(5, TimeUnit.SECONDS)).isTrue();

            assertThat(read(wallets)).isEqualTo(OPENING);

            release.countDown();
            assertThat(moving.get(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void aReadDoesNotWaitForATransferInProgress() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, pauseFirst)) {
            wallets.transfer("ben", "ana", 20);
            assertThat(paused.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Map<String, Long>> read = reader.submit(wallets::balances);
            boolean answered;
            try {
                read.get(2, TimeUnit.SECONDS);
                answered = true;
            } catch (java.util.concurrent.TimeoutException e) {
                answered = false;
            }
            assertThat(answered).as("balances() answered while a transfer was half applied").isTrue();
        }
    }

    @Test
    void balancesAlreadyHandedOutStayAsTheyWere() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, () -> { })) {
            Map<String, Long> before = read(wallets);

            assertThat(wallets.transfer("ana", "ben", 30).get(5, TimeUnit.SECONDS)).isTrue();

            assertThat(before).isEqualTo(OPENING);
        }
    }

    @Test
    void oneReaderCannotChangeWhatAnotherSees() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, () -> { })) {
            Map<String, Long> mine = read(wallets);
            try {
                mine.put("ana", 1_000_000L);
            } catch (UnsupportedOperationException expected) {
                // a read-only map is one way to keep it
            }

            assertThat(read(wallets)).isEqualTo(OPENING);
        }
    }
}
