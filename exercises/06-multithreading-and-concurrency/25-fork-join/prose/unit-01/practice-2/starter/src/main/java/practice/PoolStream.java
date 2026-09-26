package practice;

import java.util.function.LongUnaryOperator;

public final class PoolStream {

    private PoolStream() {
    }

    /** Sums f over the values with a parallel stream run inside a new pool of the given parallelism. */
    public static long sum(long[] values, int parallelism, LongUnaryOperator f) {
        throw new UnsupportedOperationException("write sum");
    }
}
