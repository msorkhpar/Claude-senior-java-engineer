package practice;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class Settings {

    private static final AtomicInteger CREATED = new AtomicInteger();

    /** Opens once two loads have started. */
    private static final CountDownLatch LOADS = new CountDownLatch(2);

    private final Map<String, String> values;

    /**
     * Loads the settings. Given, and not to be changed: a load takes two seconds, and
     * it finishes early when a second load starts, which a singleton must never allow.
     */
    private Settings() {
        LOADS.countDown();
        try {
            LOADS.await(2, TimeUnit.SECONDS);
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
