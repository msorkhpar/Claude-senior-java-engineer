package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class IdBatchesTest {

    private static final int MAX = Integer.MAX_VALUE;

    /**
     * Runs forEachBatch on a daemon thread and collects the batches. A walk that hands over more
     * batches than the range can hold is stopped, so a loop that never ends fails instead of hanging.
     */
    private static List<String> batches(int firstId, int lastId, int size, int expected) throws Exception {
        List<String> seen = new ArrayList<>();
        ExecutorService runner = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            Future<?> walk = runner.submit(() -> IdBatches.forEachBatch(firstId, lastId, size, (from, to) -> {
                seen.add(from + ".." + to);
                if (seen.size() > expected + 2) throw new IllegalStateException("too many batches: " + seen);
            }));
            try {
                walk.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof IllegalStateException ise && ise.getMessage().startsWith("too many"))) throw e;
            }
            return seen;
        } finally {
            runner.shutdownNow();
        }
    }

    @Test
    void coversTheRangeInBatchesOfAtMostTheSize() throws Exception {
        assertThat(batches(101, 115, 5, 3)).containsExactly("101..105", "106..110", "111..115");
        assertThat(batches(40, 57, 6, 3)).containsExactly("40..45", "46..51", "52..57");
    }

    @Test
    void aRangeThatEndsAtTheLargestIdEndsToo() throws Exception {
        assertThat(batches(MAX - 10, MAX, 4, 3)).containsExactly(
                (MAX - 10) + ".." + (MAX - 7), (MAX - 6) + ".." + (MAX - 3), (MAX - 2) + ".." + MAX);
    }

    @Test
    void aLastBatchOfOneIdIsHandedOver() throws Exception {
        assertThat(batches(7, 15, 4, 3)).containsExactly("7..10", "11..14", "15..15");
    }
}
