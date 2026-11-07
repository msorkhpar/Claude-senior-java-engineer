package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.IntToLongFunction;

/**
 * Compresses a list of files on a ForkJoinPool. A batch of files whose bytes fit the budget is
 * compressed by one task; a larger batch is cut in two.
 */
public class ArchiveJob {

    /** Told about every batch the job compresses in one task, and every cut it makes. */
    public interface Probe {
        void batch(int from, int to);

        void cut(int from, int mid, int to);
    }

    private final ForkJoinPool pool;
    private final long[] bytes;
    private final long budget;
    private final IntToLongFunction compress;
    private final Probe probe;

    /**
     * @param bytes    the size of each file, in archive order
     * @param budget   the most bytes one task should compress
     * @param compress compresses file i and returns its compressed size
     */
    public ArchiveJob(ForkJoinPool pool, long[] bytes, long budget, IntToLongFunction compress, Probe probe) {
        this.pool = pool;
        this.bytes = bytes.clone();
        this.budget = budget;
        this.compress = compress;
        this.probe = probe;
    }

    /** Compresses every file and returns the archive's compressed size. */
    public long run() {
        return pool.invoke(new Batch(0, bytes.length));
    }

    /** Bytes of files from (inclusive) to to (exclusive). */
    long bytesIn(int from, int to) {
        long s = 0;
        for (int i = from; i < to; i++) s += bytes[i];
        return s;
    }

    /** Whether the files [from, to) are compressed by one task. */
    boolean isLeaf(int from, int to) {
        return bytesIn(from, to) <= budget;
    }

    /**
     * Where to cut the files [from, to), a batch that is not a leaf: the first file of the second
     * half. Both halves hold at least one file.
     */
    int splitPoint(int from, int to) {
        long half = bytesIn(from, to) / 2;
        long seen = 0;
        int mid = from;
        while (mid < to && seen < half) seen += bytes[mid++];
        return Math.max(from + 1, Math.min(mid, to - 1));
    }

    private final class Batch extends RecursiveTask<Long> {
        private final int from, to;

        Batch(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        protected Long compute() {
            if (isLeaf(from, to)) {
                probe.batch(from, to);
                long s = 0;
                for (int i = from; i < to; i++) s += compress.applyAsLong(i);
                return s;
            }
            int mid = splitPoint(from, to);
            probe.cut(from, mid, to);
            Batch left = new Batch(from, mid);
            left.fork();
            long r = new Batch(mid, to).compute();
            return left.join() + r;
        }
    }
}
