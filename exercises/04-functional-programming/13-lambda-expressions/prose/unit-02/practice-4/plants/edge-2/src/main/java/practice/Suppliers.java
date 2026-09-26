package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Suppliers {

    private Suppliers() {
    }

    /** {@code value}, or the fallback's result when {@code value} is null. */
    public static String valueOr(String value, Supplier<String> fallback) {
        return value != null ? value : fallback.get();
    }

    /** A supplier whose every get() gives an empty, modifiable list. */
    public static Supplier<List<String>> freshLists() {
        List<String> list = new ArrayList<>();
        return () -> list;
    }
}
