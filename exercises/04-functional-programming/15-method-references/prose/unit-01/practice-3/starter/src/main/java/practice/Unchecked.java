package practice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Function;

/** A function whose apply may throw IOException. */
@FunctionalInterface
interface IoFunction<T, R> {
    R apply(T t) throws IOException;
}

public final class Unchecked {

    private Unchecked() {
    }

    /** A Function that calls fn and rethrows an IOException as an UncheckedIOException. */
    public static <T, R> Function<T, R> function(IoFunction<T, R> fn) {
        throw new UnsupportedOperationException("write function");
    }

    /** Applies fn to every item, in order. */
    public static <T, R> List<R> mapAll(List<T> items, IoFunction<T, R> fn) {
        throw new UnsupportedOperationException("write mapAll");
    }
}
