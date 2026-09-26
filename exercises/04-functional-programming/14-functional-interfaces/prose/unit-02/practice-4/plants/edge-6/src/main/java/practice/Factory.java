package practice;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Factory {

    private Factory() {
    }

    public static <T> List<T> createN(int n, Supplier<T> factory) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative: " + n);
        }
        return Stream.generate(factory).parallel().limit(n).collect(Collectors.toList());
    }
}
