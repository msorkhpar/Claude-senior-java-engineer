package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Turns the reason code of a rejected bank transfer, as the bank sends it, into the reason the
 * payments team works with.
 */
public class Rejections {

    public enum Reason {
        INCORRECT_ACCOUNT("AC01"),
        CLOSED_ACCOUNT("AC04"),
        BLOCKED_ACCOUNT("AC06"),
        INSUFFICIENT_FUNDS("AM04"),
        DUPLICATE_PAYMENT("AM05"),
        REGULATORY("RR04"),
        NOT_SPECIFIED("MS03");

        private static final Map<String, Reason> BY_CODE = index(values(), Reason::code);

        private final String code;

        Reason(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        /** The reason the bank means by {@code code}, if the code is one we know. */
        public static Optional<Reason> fromCode(String code) {
            return Optional.of(BY_CODE.getOrDefault(code, NOT_SPECIFIED));
        }
    }

    /**
     * Indexes {@code items} by the code {@code codeOf} gives each one. Two items with the same code
     * are a mistake in the table: refuse them with an IllegalArgumentException that names the code.
     */
    static <T> Map<String, T> index(T[] items, Function<T, String> codeOf) {
        Map<String, T> byCode = new HashMap<>();
        for (T item : items) {
            T before = byCode.putIfAbsent(codeOf.apply(item), item);
            if (before != null) throw new IllegalArgumentException("duplicate code " + codeOf.apply(item));
        }
        return Map.copyOf(byCode);
    }
}
