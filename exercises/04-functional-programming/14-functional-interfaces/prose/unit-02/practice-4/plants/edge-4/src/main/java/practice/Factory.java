package practice;

import java.util.ArrayList;
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
        if (n == 0) {
            return new ArrayList<>();
        }
        T first = factory.get();
        if (first == null) {
            throw new IllegalArgumentException("factory returned null");
        }
        return Stream.generate(factory).limit(n).collect(Collectors.toList());
    }
}
