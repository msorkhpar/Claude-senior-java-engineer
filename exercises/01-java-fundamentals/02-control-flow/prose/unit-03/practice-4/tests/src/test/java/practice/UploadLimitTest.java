package practice;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntPredicate;

import static org.assertj.core.api.Assertions.assertThat;

class UploadLimitTest {

    /** A gateway that accepts up to limit bytes; it refuses probes outside 1..maxBytes and runaway searches. */
    private static IntPredicate gateway(int maxBytes, int limit) {
        int[] probes = new int[1];
        return size -> {
            if (size < 1 || size > maxBytes) {
                throw new IllegalArgumentException("probed " + size + " bytes, outside 1.." + maxBytes);
            }
            if (++probes[0] > 64) throw new IllegalStateException("more than 64 probes");
            return size <= limit;
        };
    }

    private static int largest(int maxBytes, int limit) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            return pool.submit(() -> UploadLimit.largestAccepted(maxBytes, gateway(maxBytes, limit)))
                    .get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aGatewayThatRefusesEveryUploadGivesZero() throws Exception {
        assertThat(largest(4_096, 0)).isZero();
    }

    @Test
    void findsTheLimitInsideASmallRange() throws Exception {
        assertThat(largest(1_000, 613)).isEqualTo(613);
    }

    @Test
    void findsTheLimitWhenTheRangeReachesTheLargestInt() throws Exception {
        assertThat(largest(Integer.MAX_VALUE, 1_900_000_123)).isEqualTo(1_900_000_123);
    }

    @Test
    void aGatewayThatAcceptsTheWholeRangeGivesItsTop() throws Exception {
        assertThat(largest(50_000, 50_000)).isEqualTo(50_000);
    }
}
