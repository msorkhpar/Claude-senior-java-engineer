package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Factory {

    private Factory() {
    }

    /** Returns n objects, each made by one call to {@code factory}; rejects a negative n. */
    public static <T> List<T> createN(int n, Supplier<T> factory) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative: " + n);
        }
        List<T> out = new ArrayList<>();
        do {
            out.add(factory.get());
        } while (out.size() < n);
        return out;
    }
}
