package practice;

import java.util.List;
import java.util.function.Supplier;

public final class Factory {

    private Factory() {
    }

    /** Returns n objects, each made by one call to {@code factory}; rejects a negative n. */
    public static <T> List<T> createN(int n, Supplier<T> factory) {
        throw new UnsupportedOperationException("write createN");
    }
}
