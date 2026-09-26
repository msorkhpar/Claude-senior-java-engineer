package practice;

import java.util.List;
import java.util.function.Function;

public final class Wrapped {

    private Wrapped() {
    }

    /** f applied to each element of a synchronized list, in order, as a new list. */
    public static <T, R> List<R> mapAll(List<T> syncList, Function<? super T, ? extends R> f) {
        throw new UnsupportedOperationException("write mapAll");
    }

    /** The synchronized list's current elements, as a new list. */
    public static <T> List<T> snapshot(List<T> syncList) {
        throw new UnsupportedOperationException("write snapshot");
    }
}
