package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class PriceImportTest {

    /** Runs the import on a daemon thread, so a wrong solution that blocks can't hang the build. */
    private static <T> T within5s(Callable<T> call) throws Throwable {
        FutureTask<T> task = new FutureTask<>(call);
        Thread t = new Thread(task, "import");
        t.setDaemon(true);
        t.start();
        try {
            return task.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    private static Map<String, String> row(String sku, String cents) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("sku", sku);
        row.put("price_cents", cents);
        return row;
    }

    private static Map<String, String> renamed(String sku, String cents) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("sku", sku);
        row.put("price", cents);
        return row;
    }

    /** Never waits; when interruptAt is n, the n-th acquire is interrupted, as a deploy would. */
    static final class Throttle implements PriceImport.Throttle {
        final AtomicInteger calls = new AtomicInteger();
        final int interruptAt;

        Throttle(int interruptAt) {
            this.interruptAt = interruptAt;
        }

        @Override
        public void acquire() throws InterruptedException {
            if (calls.incrementAndGet() == interruptAt) throw new InterruptedException();
        }
    }

    @Test
    void importsTheReadableRowsAndCountsTheUnreadableOnes() throws Throwable {
        Map<String, Long> catalog = new LinkedHashMap<>();
        Throttle throttle = new Throttle(0);
        PriceImport job = new PriceImport(throttle, catalog::put);

        PriceImport.Result result = within5s(() -> job.importAll(List.of(
                row("TEA-0042", "1250"),
                row("TEA-0043", " 899 "),
                row("MUG-0007", "12.99"),
                row("MUG-0008", "n/a"),
                row("JAR-0311", "40500"))));

        assertThat(result).isEqualTo(new PriceImport.Result(3, 2));
        assertThat(catalog).containsExactly(
                Map.entry("TEA-0042", 1250L), Map.entry("TEA-0043", 899L), Map.entry("JAR-0311", 40500L));
        assertThat(throttle.calls.get()).isEqualTo(3);
    }

    @Test
    void aFileWithoutThePriceColumnFailsTheImport() throws Throwable {
        Map<String, Long> catalog = new LinkedHashMap<>();
        PriceImport job = new PriceImport(new Throttle(0), catalog::put);
        assertThat(within5s(() -> job.importAll(List.of(row("CUP-0900", "x"), row("CUP-0901", "310")))))
                .isEqualTo(new PriceImport.Result(1, 1));
        catalog.clear();

        Throwable thrown = catchThrowable(() -> within5s(() -> job.importAll(List.of(
                renamed("TEA-0042", "1250"), renamed("TEA-0043", "899")))));

        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(catalog).isEmpty();
    }

    @Test
    void anInterruptEndsTheImportAndReachesTheJobRunner() {
        Map<String, Long> catalog = new LinkedHashMap<>();
        PriceImport job = new PriceImport(new Throttle(3), catalog::put);

        Throwable thrown = catchThrowable(() -> within5s(() -> job.importAll(List.of(
                row("PEN-0100", "150"), row("PEN-0101", "175"), row("PEN-0102", "200"),
                row("PEN-0103", "225"), row("PEN-0104", "250")))));

        assertThat(thrown).isInstanceOf(InterruptedException.class);
        assertThat(catalog).containsOnlyKeys("PEN-0100", "PEN-0101");
    }
}
