package practice;

import java.util.List;

public final class DeprecationReport {

    private DeprecationReport() {
    }

    /** One sorted line per deprecated method or constructor the type itself declares. */
    public static List<String> of(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
