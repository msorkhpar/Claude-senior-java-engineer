package practice;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class Settings {

    private static final AtomicInteger CREATED = new AtomicInteger();

    private final Map<String, String> values;

    /** Loads the settings; slow on purpose, and not to be changed. */
    private Settings() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        CREATED.incrementAndGet();
        values = Map.of("mode", "prod", "region", "eu");
    }

    /** How many times the constructor has run. */
    public static int created() {
        return CREATED.get();
    }

    /** Loaded on first use of the holder, which the JVM does exactly once. */
    private static final class Holder {
        private static final Settings INSTANCE = new Settings();
    }

    public static Settings getInstance() {
        return Holder.INSTANCE;
    }

    public String get(String key) {
        return values.get(key);
    }
}
