package practice;

import java.util.List;

public final class Tracing {

    private Tracing() {
    }

    public interface Store {
        String get(String key);

        String put(String key, String value);

        int size();
    }

    /** Returns a proxy of {@code type} that logs each call into {@code log} and delegates it to {@code target}. */
    public static <T> T logging(T target, Class<T> type, List<String> log) {
        throw new UnsupportedOperationException("write logging");
    }
}
