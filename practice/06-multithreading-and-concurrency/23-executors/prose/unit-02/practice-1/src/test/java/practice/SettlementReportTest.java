package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementReportTest {

    private static final ThreadFactory DAEMONS = r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    };

    private final ExecutorService pool = Executors.newFixedThreadPool(4, DAEMONS);
    private final ExecutorService caller = Executors.newSingleThreadExecutor(DAEMONS);
    private final CountDownLatch release = new CountDownLatch(1);

    @AfterEach
    void cleanUp() {
        release.countDown();
        pool.shutdownNow();
        caller.shutdownNow();
    }

    /** Runs the report on a thread of its own, so a report that never returns can't hang the test. */
    private List<String> report(SettlementReport.Bank bank, List<String> merchants) throws Exception {
        Future<List<String>> lines = caller.submit(() -> new SettlementReport(pool, bank).run(merchants));
        return lines.get(5, TimeUnit.SECONDS);
    }

    @Test
    void everySettledMerchantGetsItsLineInOrder() throws Exception {
        Map<String, Long> amounts = Map.of("m-731", 120_450L, "m-052", 8_800L, "m-419", 0L);

        List<String> lines = report(amounts::get, List.of("m-731", "m-052", "m-419"));

        assertThat(lines).containsExactly("m-731: settled 120450", "m-052: settled 8800", "m-419: settled 0");
    }

    @Test
    void aBankErrorIsReportedAsWhatTheBankCallThrew() throws Exception {
        SettlementReport.Bank bank = merchant -> {
            if (merchant.equals("m-052")) throw new SocketTimeoutException("bank-3 did not answer in 30 s");
            return 4_200L;
        };

        List<String> lines = report(bank, List.of("m-052"));

        assertThat(lines).containsExactly("m-052: failed: SocketTimeoutException: bank-3 did not answer in 30 s");
    }

    @Test
    void oneFailedMerchantDoesNotCostTheOthersTheirLines() throws Exception {
        SettlementReport.Bank bank = merchant -> {
            if (merchant.equals("m-208")) throw new SQLTransientConnectionException("ledger pool exhausted");
            return 15_000L;
        };

        List<String> lines = report(bank, List.of("m-731", "m-208", "m-419"));

        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).isEqualTo("m-731: settled 15000");
        assertThat(lines.get(1)).startsWith("m-208: failed: ");
        assertThat(lines.get(2)).isEqualTo("m-419: settled 15000");
    }

    @Test
    void interruptingTheReportEndsItRatherThanFailingTheMerchants() throws Exception {
        CountDownLatch bothAtTheBank = new CountDownLatch(2);
        SettlementReport.Bank slowBank = merchant -> {
            bothAtTheBank.countDown();
            release.await();
            return 1L;
        };
        AtomicReference<Thread> reportThread = new AtomicReference<>();
        Future<List<String>> lines = caller.submit(() -> {
            reportThread.set(Thread.currentThread());
            return new SettlementReport(pool, slowBank).run(List.of("m-600", "m-601"));
        });
        assertThat(bothAtTheBank.await(5, TimeUnit.SECONDS)).isTrue();

        reportThread.get().interrupt();

        Throwable ended = null;
        try {
            lines.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            ended = e.getCause();
        } catch (java.util.concurrent.TimeoutException e) {
            ended = e;
        }
        assertThat(ended).isInstanceOf(InterruptedException.class);
    }
}
