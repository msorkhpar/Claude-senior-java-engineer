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

    private static Settings instance;

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public String get(String key) {
        return values.get(key);
    }
}
