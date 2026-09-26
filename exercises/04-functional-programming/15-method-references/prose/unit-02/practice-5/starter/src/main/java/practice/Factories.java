package practice;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Factories {

    private Factories() {
    }

    /** count objects made by factory. */
    public static <C> List<C> buckets(int count, Supplier<C> factory) {
        throw new UnsupportedOperationException("write buckets");
    }

    /** One object per name, made by factory, in order. */
    public static <T> List<T> build(List<String> names, Function<String, T> factory) {
        throw new UnsupportedOperationException("write build");
    }

    /** The words upper-cased, as a String[]. */
    public static String[] shout(List<String> words) {
        throw new UnsupportedOperationException("write shout");
    }
}
