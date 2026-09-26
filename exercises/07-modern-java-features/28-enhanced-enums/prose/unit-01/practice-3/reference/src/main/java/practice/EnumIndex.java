package practice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EnumIndex {

    private EnumIndex() {
    }

    public enum Dial {
        US(1), UK(44), DE(49), IE(353), FI(358);

        private final int code;

        Dial(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    /** The United States and Canada share the code 1. */
    public enum SharedDial {
        US(1), CA(1), UK(44);

        private final int code;

        SharedDial(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    /** Builds an index of {@code type}'s constants by {@code key}, and returns a lookup into it. */
    public static <E extends Enum<E>, K> Function<K, Optional<E>> indexBy(Class<E> type, Function<E, K> key) {
        Map<K, E> index = Arrays.stream(type.getEnumConstants())
                .collect(Collectors.toUnmodifiableMap(key, Function.identity()));
        return k -> Optional.ofNullable(index.get(k));
    }
}
