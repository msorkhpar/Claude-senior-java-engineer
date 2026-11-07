package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class ImportAuditTest {

    /** A row is valid when it has three fields and a numeric quantity. */
    private static final ImportAudit.RowCheck CHECK = row -> {
        String[] f = row.split(";");
        return f.length == 3 && f[2].matches("\\d+");
    };

    private final ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    private static ImportAudit.RowSource rows(String... rows) {
        return action -> List.of(rows).forEach(action);
    }

    @Test
    void countsTheRowsTheCheckRejects() throws Exception {
        ImportAudit audit = new ImportAudit(CHECK);

        Future<Integer> result = pool.submit(() -> audit.rejected(rows(
                "SKU-17;bolts;400", "SKU-18;nuts", "SKU-19;washers;lots", "SKU-20;rivets;75", ";;")));

        assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo(3);
    }

    @Test
    void eachImportIsCountedFromZero() throws Exception {
        ImportAudit audit = new ImportAudit(CHECK);

        int first = pool.submit(() -> audit.rejected(rows("A;x;1", "B;y", "C;z;q"))).get(5, TimeUnit.SECONDS);
        int second = pool.submit(() -> audit.rejected(rows("D;w;9", "E;v;oops"))).get(5, TimeUnit.SECONDS);

        assertThat(first).isEqualTo(2);
        assertThat(second).isEqualTo(1);
    }

    @Test
    void twoImportsAuditedAtTheSameTimeEachGetTheirOwnCount() throws Exception {
        ImportAudit audit = new ImportAudit(CHECK);
        CountDownLatch halfway = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        ImportAudit.RowSource slowFile = action -> {
            action.accept("K-1;ok;5");
            action.accept("K-2;broken");
            halfway.countDown();                                   // the file is still being read
            try {
                if (!resume.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("never resumed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            action.accept("K-3;broken");
        };

        Future<Integer> slow = pool.submit(() -> audit.rejected(slowFile));
        assertThat(halfway.await(5, TimeUnit.SECONDS)).isTrue();
        int quick = pool.submit(() -> audit.rejected(rows("Q-1;a", "Q-2;b", "Q-3;c;1", "Q-4;d")))
                .get(5, TimeUnit.SECONDS);
        resume.countDown();

        assertThat(quick).isEqualTo(3);
        assertThat(slow.get(5, TimeUnit.SECONDS)).isEqualTo(2);
    }
}
