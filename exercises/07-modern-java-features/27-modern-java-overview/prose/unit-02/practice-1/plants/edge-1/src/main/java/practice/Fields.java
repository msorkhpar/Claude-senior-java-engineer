package practice;

import java.util.List;

public final class Fields {

    private Fields() {
    }

    /** The values with surrounding whitespace removed, blank values left out. */
    public static List<String> clean(List<String> raw) {
        return raw.stream()
                .filter(value -> !value.isBlank())
                .map(String::trim)
                .toList();
    }
}
