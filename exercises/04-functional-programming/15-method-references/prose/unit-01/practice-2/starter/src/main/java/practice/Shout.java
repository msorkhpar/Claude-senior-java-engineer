package practice;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** One changeable string. */
final class Holder {

    private String value;

    Holder(String value) {
        this.value = value;
    }

    String get() {
        return value;
    }

    void set(String value) {
        this.value = value;
    }
}

public final class Shout {

    private Shout() {
    }

    /** Upper-cases the holder's value as it is now; a null value fails here. */
    public static Supplier<String> of(Holder holder) {
        throw new UnsupportedOperationException("write of");
    }

    /** Upper-cases whatever string it is given. */
    public static Function<String, String> each() {
        throw new UnsupportedOperationException("write each");
    }

    /** Upper-cases every word, in order. */
    public static List<String> all(List<String> words) {
        throw new UnsupportedOperationException("write all");
    }
}
