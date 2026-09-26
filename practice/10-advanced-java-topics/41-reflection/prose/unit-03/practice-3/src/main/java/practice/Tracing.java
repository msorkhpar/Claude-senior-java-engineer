package practice;

import java.util.List;

public final class Tracing {

    private Tracing() {
    }

    /** A proxy of iface that calls target and logs every call and its outcome. */
    public static <T> T trace(Class<T> iface, T target, List<String> log) {
        throw new UnsupportedOperationException("TODO");
    }
}
