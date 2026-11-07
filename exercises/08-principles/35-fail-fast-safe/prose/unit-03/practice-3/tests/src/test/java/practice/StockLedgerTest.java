package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import practice.StockLedger.Line;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class StockLedgerTest {

    private final ExecutorService reporter = daemonPool("report");
    private final ExecutorService transfers = daemonPool("transfer");
    private final CountDownLatch release = new CountDownLatch(1);

    private static ExecutorService daemonPool(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }

    @AfterEach
    void cleanUp() {
        release.countDown();
        reporter.shutdownNow();
        transfers.shutdownNow();
    }

    private static Map<String, Integer> opening() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("Leeds", 340);
        m.put("Porto", 125);
        m.put("Graz", 410);
        m.put("Bergen", 75);
        m.put("Lyon", 250);
        m.put("Turku", 60);
        return m;
    }

    /** A printer that signals at its first line and holds the report there until released. */
    private Consumer<Line> slowPrinter(CountDownLatch reached, List<Line> printed) {
        return line -> {
            printed.add(line);
            reached.countDown();
            try {
                if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("never released");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    @Test
    void aReportListsEveryWarehouseAfterTheMoves() throws Exception {
        StockLedger ledger = new StockLedger(opening());
        ledger.move("Graz", "Turku", 90);
        ledger.move("Leeds", "Porto", 15);
        List<Line> printed = new ArrayList<>();

        long total = reporter.submit(() -> ledger.report(printed::add)).get(5, TimeUnit.SECONDS);

        assertThat(total).isEqualTo(1_260);
        assertThat(printed).containsExactlyInAnyOrder(new Line("Leeds", 325), new Line("Porto", 140),
                new Line("Graz", 320), new Line("Bergen", 75), new Line("Lyon", 250), new Line("Turku", 150));
    }

    @Test
    void aReportAddsUpWhileUnitsMoveDuringIt() throws Exception {
        StockLedger ledger = new StockLedger(opening());
        List<String> order = new ArrayList<>();
        reporter.submit(() -> ledger.report(line -> order.add(line.warehouse()))).get(5, TimeUnit.SECONDS);

        CountDownLatch reached = new CountDownLatch(1);
        List<Line> printed = new CopyOnWriteArrayList<>();
        Future<Long> report = reporter.submit(() -> ledger.report(slowPrinter(reached, printed)));
        assertThat(reached.await(5, TimeUnit.SECONDS)).as("the report printed its first line").isTrue();
        String printedFirst = printed.get(0).warehouse();
        String later = order.get(order.size() - 1).equals(printedFirst) ? order.get(0) : order.get(order.size() - 1);

        Future<?> move = transfers.submit(() -> ledger.move(printedFirst, later, 30));
        try {
            move.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // another case checks that moves don't wait; here only the report matters
        }
        release.countDown();
        long total = report.get(5, TimeUnit.SECONDS);

        assertThat(total).as("the total of a report taken while 30 units moved").isEqualTo(1_260);
        assertThat(printed.stream().mapToLong(Line::units).sum()).as("the printed lines").isEqualTo(1_260);
    }

    @Test
    void aMoveDoesNotWaitForTheReportToPrint() throws Exception {
        StockLedger ledger = new StockLedger(opening());
        CountDownLatch reached = new CountDownLatch(1);
        reporter.submit(() -> ledger.report(slowPrinter(reached, new CopyOnWriteArrayList<>())));
        assertThat(reached.await(5, TimeUnit.SECONDS)).as("the report printed its first line").isTrue();

        Future<?> move = transfers.submit(() -> ledger.move("Lyon", "Bergen", 40));
        try {
            move.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("move() was still waiting while the report was printing");
        }
    }
}
