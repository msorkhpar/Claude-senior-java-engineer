package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteBoardTest {

    private static final ThreadFactory DAEMONS = r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    };

    private final ExecutorService pool = Executors.newFixedThreadPool(4, DAEMONS);
    private final ExecutorService page = Executors.newSingleThreadExecutor(DAEMONS);
    private final CountDownLatch carrierWakesUp = new CountDownLatch(1);
    private final CountDownLatch carrierInterrupted = new CountDownLatch(1);

    @AfterEach
    void cleanUp() {
        carrierWakesUp.countDown();
        pool.shutdownNow();
        page.shutdownNow();
    }

    /** A carrier that never answers unless the test lets it, and notes when it is interrupted. */
    private final Callable<Long> silent = () -> {
        try {
            carrierWakesUp.await();
        } catch (InterruptedException e) {
            carrierInterrupted.countDown();
            throw e;
        }
        return 1L;
    };

    /** Builds the board on a thread of its own, so a board that never returns can't hang the test. */
    private List<String> board(long deadlineMillis, Map<String, Callable<Long>> carriers) throws Exception {
        Future<List<String>> lines = page.submit(() -> new QuoteBoard(pool, deadlineMillis).collect(carriers));
        return lines.get(5, TimeUnit.SECONDS);
    }

    @Test
    void showsEveryQuoteAndEveryFailedCarrierInOrder() throws Exception {
        Map<String, Callable<Long>> carriers = new LinkedHashMap<>();
        carriers.put("tidewater", () -> 22_150L);
        carriers.put("bluecargo", () -> { throw new ConnectException("api.bluecargo.example: connection refused"); });
        carriers.put("ostrava-rail", () -> 19_800L);

        assertThat(board(5_000, carriers))
                .containsExactly("tidewater: 22150", "bluecargo: no quote", "ostrava-rail: 19800");
    }

    @Test
    void aCarrierThatNeverAnswersDoesNotHoldUpTheBoard() throws Exception {
        Map<String, Callable<Long>> carriers = new LinkedHashMap<>();
        carriers.put("redroute", silent);

        assertThat(board(200, carriers)).containsExactly("redroute: too late");
    }

    @Test
    void aLateCarrierIsShownAsLateAndTheCarriersAfterItStillShow() throws Exception {
        Map<String, Callable<Long>> carriers = new LinkedHashMap<>();
        carriers.put("redroute", silent);
        carriers.put("tidewater", () -> 22_150L);

        List<String> lines = board(500, carriers);

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("redroute: too late");
        assertThat(lines.get(1)).startsWith("tidewater: ");
    }

    @Test
    void aLateCarrierIsStoppedWhenTheDeadlineComes() throws Exception {
        Map<String, Callable<Long>> carriers = new LinkedHashMap<>();
        carriers.put("redroute", silent);

        board(200, carriers);

        assertThat(carrierInterrupted.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
