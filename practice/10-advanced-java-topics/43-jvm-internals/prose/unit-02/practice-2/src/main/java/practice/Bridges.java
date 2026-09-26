package practice;

import java.util.List;

public final class Bridges {

    private Bridges() {
    }

    /** The bridge methods {@code type} declares, as "ReturnType name(Param, ...)", in any order. */
    public static List<String> bridgesOf(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The methods the source of {@code type} declares (no synthetic ones), in the same form. */
    public static List<String> sourceMethodsOf(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
