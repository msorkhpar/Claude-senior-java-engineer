package practice;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Compositions {

    private Compositions() {
    }

    public static <T> Function<T, T> inOrder(List<Function<T, T>> steps) {
        throw new UnsupportedOperationException("write inOrder");
    }

    public static Function<String, String> lengthLabel() {
        throw new UnsupportedOperationException("write lengthLabel");
    }

    public static Predicate<String> acceptedName() {
        throw new UnsupportedOperationException("write acceptedName");
    }
}
