package practice;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Factory {

    private Factory() {
    }

    /** Returns n objects, each made by one call to {@code factory}; rejects a negative n. */
    public static <T> List<T> createN(int n, Supplier<T> factory) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative: " + n);
        }
        return Stream.generate(factory).limit(n).collect(Collectors.toList());
    }
}
