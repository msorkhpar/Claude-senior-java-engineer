package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceSweepTest {

    /** Sweeps on a daemon thread; the visitor refuses ids outside the shard and runaway sweeps. */
    private static List<Integer> visited(int first, int last) throws Exception {
        List<Integer> ids = new ArrayList<>();
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            pool.submit(() -> InvoiceSweep.forEachId(first, last, id -> {
                if (id < first || id > last) throw new IllegalStateException("visited id " + id
                        + " outside " + first + ".." + last);
                if (ids.size() > 100) throw new IllegalStateException("more than 100 ids visited");
                ids.add(id);
            })).get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        return ids;
    }

    @Test
    void aShardInTheMiddleIsSweptOnceInOrder() throws Exception {
        assertThat(visited(40_017, 40_021)).containsExactly(40_017, 40_018, 40_019, 40_020, 40_021);
    }

    @Test
    void aShardOfOneInvoiceIsSwept() throws Exception {
        assertThat(visited(912, 912)).containsExactly(912);
    }

    @Test
    void theLastShardEndsAtTheLargestId() throws Exception {
        int top = Integer.MAX_VALUE;
        assertThat(visited(top - 3, top)).containsExactly(top - 3, top - 2, top - 1, top);
    }

    @Test
    void anEmptyShardIsNotSwept() throws Exception {
        assertThat(visited(7_001, 7_000)).isEmpty();
    }
}
