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

    private static final Map<String, Long> OPENING = Map.of("ana", 50L, "ben", 20L, "cy", 0L, "dee", 5L);

    /** Requests go through here, so a call that blocks fails its test instead of hanging it. */
    private final ExecutorService caller = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "test-caller");
        t.setDaemon(true);
        return t;
    });

    /** Holds the first transfer between taking and adding until released. */
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
        caller.shutdownNow();
    }

    private Future<Boolean> request(Wallets wallets, String from, String to, long credits) throws Exception {
        return caller.submit(() -> wallets.transfer(from, to, credits)).get(2, TimeUnit.SECONDS);
    }

    private static boolean outcome(Future<Boolean> transfer) throws Exception {
        return transfer.get(5, TimeUnit.SECONDS);
    }

    @Test
    void transfersMoveCreditsAndRefuseWhatAWalletCannotCover() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, () -> { })) {
            assertThat(outcome(request(wallets, "ana", "cy", 15))).isTrue();
            assertThat(outcome(request(wallets, "dee", "ben", 6))).isFalse();
            assertThat(outcome(request(wallets, "ben", "dee", 20))).isTrue();
            assertThat(outcome(request(wallets, "cy", "ana", 15))).isTrue();

            assertThat(wallets.balances()).isEqualTo(Map.of("ana", 50L, "ben", 0L, "cy", 0L, "dee", 25L));
        }
    }

    @Test
    void twoTransfersThatFitOnlyOneAtATimeAreNotBothApplied() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, pauseFirst)) {
            Future<Boolean> earlier = request(wallets, "dee", "cy", 5);
            assertThat(paused.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Boolean> toBen = request(wallets, "ana", "ben", 30);
            Future<Boolean> toCy = request(wallets, "ana", "cy", 30);
            release.countDown();

            assertThat(outcome(earlier)).isTrue();
            assertThat(outcome(toBen) && outcome(toCy)).as("both 30-credit transfers from 50").isFalse();
            assertThat(wallets.balances().get("ana")).isEqualTo(20L);
        }
    }

    @Test
    void aTransferCanSpendCreditsFromTheOneBeforeIt() throws Exception {
        try (Wallets wallets = new Wallets(OPENING, pauseFirst)) {
            Future<Boolean> intoCy = request(wallets, "ana", "cy", 15);
            assertThat(paused.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Boolean> outOfCy = request(wallets, "cy", "ben", 10);
            release.countDown();

            assertThat(outcome(intoCy)).isTrue();
            assertThat(outcome(outOfCy)).isTrue();
            assertThat(wallets.balances()).isEqualTo(Map.of("ana", 35L, "ben", 30L, "cy", 5L, "dee", 5L));
        }
    }
}
