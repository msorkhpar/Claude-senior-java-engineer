package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.IntToLongFunction;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveJobTest {

    private ForkJoinPool pool;
    private final List<int[]> batches = Collections.synchronizedList(new ArrayList<>());
    private final List<int[]> cuts = Collections.synchronizedList(new ArrayList<>());

    private final ArchiveJob.Probe probe = new ArchiveJob.Probe() {
        @Override
        public void batch(int from, int to) {
            batches.add(new int[] {from, to});
        }

        @Override
        public void cut(int from, int mid, int to) {
            cuts.add(new int[] {from, mid, to});
        }
    };

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(3);
    }

    @AfterEach
    void closePool() {
        pool.shutdownNow();
    }

    /** Runs the call on a daemon thread and waits for it at most 30 seconds. */
    private static <T> T within30s(Callable<T> call) throws Exception {
        FutureTask<T> f = new FutureTask<>(call);
        Thread t = new Thread(f, "test-caller");
        t.setDaemon(true);
        t.start();
        try {
            return f.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception x) throw x;
            if (e.getCause() instanceof Error x) throw new AssertionError("the job failed with " + x, x);
            throw e;
        }
    }

    /** A made-up compressed size: about a third, plus a header. */
    private static IntToLongFunction compressing(long[] bytes) {
        return i -> bytes[i] / 3 + 64;
    }

    private static long expected(long[] bytes) {
        long s = 0;
        for (long b : bytes) s += b / 3 + 64;
        return s;
    }

    private long run(long[] bytes, long budget) throws Exception {
        batches.clear();
        cuts.clear();
        ArchiveJob job = new ArchiveJob(pool, bytes, budget, compressing(bytes), probe);
        return within30s(job::run);
    }

    private static long sum(long[] bytes, int from, int to) {
        long s = 0;
        for (int i = from; i < to; i++) s += bytes[i];
        return s;
    }

    private static long max(long[] bytes, int from, int to) {
        long m = 0;
        for (int i = from; i < to; i++) m = Math.max(m, bytes[i]);
        return m;
    }

    /** Files no bigger than half the budget, of very different sizes. */
    private static long[] mixed(int n) {
        long[] b = new long[n];
        for (int i = 0; i < n; i++) b[i] = 1 + (i * 7_919L) % 4_000 + (i % 17 == 0 ? 6_000 : 0);
        return b;
    }

    @Test
    void compressesEveryFileOnce() throws Exception {
        for (int n : new int[] {0, 1, 2, 5, 60, 2_345}) {
            long[] bytes = mixed(n);
            assertThat(run(bytes, 25_000)).as("%d files", n).isEqualTo(expected(bytes));

            List<int[]> sorted = new ArrayList<>(batches);
            sorted.sort(Comparator.comparingInt(r -> r[0]));
            int next = 0;
            for (int[] r : sorted) {
                if (r[0] == r[1]) continue;
                assertThat(r[0]).as("batches of %d files follow each other", n).isEqualTo(next);
                assertThat(r[1] - r[0] == 1 || sum(bytes, r[0], r[1]) <= 25_000)
                        .as("batch [%d, %d) fits the budget", r[0], r[1]).isTrue();
                next = r[1];
            }
            assertThat(next).as("every one of %d files is in a batch", n).isEqualTo(n);
        }
    }

    @Test
    void eachCutDividesTheBytesEvenly() throws Exception {
        long[] bytes = new long[60];
        for (int i = 0; i < 60; i++) bytes[i] = i < 45 ? 2 + i % 3 : 70 + i % 5;   // big files at the end
        assertThat(run(bytes, 150)).isEqualTo(expected(bytes));

        assertThat(cuts).isNotEmpty();
        for (int[] c : cuts) {
            long whole = sum(bytes, c[0], c[2]);
            long bigger = Math.max(sum(bytes, c[0], c[1]), sum(bytes, c[1], c[2]));
            assertThat(2 * bigger)
                    .as("cut of [%d, %d) at %d: halves of %d and %d bytes", c[0], c[2], c[1],
                            sum(bytes, c[0], c[1]), sum(bytes, c[1], c[2]))
                    .isLessThanOrEqualTo(whole + 2 * max(bytes, c[0], c[2]));
        }
    }

    @Test
    void aBatchEndingInAHugeFileIsCut() throws Exception {
        long[] bytes = {3, 4, 5, 200};
        assertThat(run(bytes, 20)).isEqualTo(expected(bytes));
        long[] more = {11, 9, 13, 8, 12, 10, 4_000};
        assertThat(run(more, 50)).isEqualTo(expected(more));
    }

    @Test
    void aFileOverTheBudgetIsABatchOfItsOwn() throws Exception {
        long[] bytes = {500, 5, 5, 5, 5};
        assertThat(run(bytes, 50)).isEqualTo(expected(bytes));
        assertThat(batches).anySatisfy(r -> assertThat(r).containsExactly(0, 1));
        long[] more = {640, 1, 2, 3};
        assertThat(run(more, 100)).isEqualTo(expected(more));
    }
}
