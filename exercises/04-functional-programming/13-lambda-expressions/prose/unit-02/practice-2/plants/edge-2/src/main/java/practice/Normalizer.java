package practice;

import java.util.List;
import java.util.function.Function;

public final class Normalizer {

    private Normalizer() {
    }

    /** null to "NULL", blank to "EMPTY", anything else trimmed and upper-cased. */
    public static Function<String, String> normalizer() {
        return s -> {
            if (s == null) {
                return "NULL";
            }
            if (s.isEmpty()) {
                return "EMPTY";
            }
            return s.trim().toUpperCase();
        };
    }

    /** Every entry of {@code texts} normalized, in order; entries may be null. */
    public static List<String> normalizeAll(List<String> texts) {
        return texts.stream().map(normalizer()).toList();
    }
}

