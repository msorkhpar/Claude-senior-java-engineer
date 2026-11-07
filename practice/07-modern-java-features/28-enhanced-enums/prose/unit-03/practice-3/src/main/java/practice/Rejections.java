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
            // TODO: Find the reason for a code. Banks add codes before we do.
            throw new IllegalStateException("TODO: fromCode");
        }
    }

    /**
     * Indexes {@code items} by the code {@code codeOf} gives each one. Two items with the same code
     * are a mistake in the table: refuse them with an IllegalArgumentException that names the code.
     */
    static <T> Map<String, T> index(T[] items, Function<T, String> codeOf) {
        // TODO: Build the index the lookup reads. A table of reasons is edited by hand when the bank
        //       publishes new codes.
        throw new IllegalStateException("TODO: index");
    }
}
