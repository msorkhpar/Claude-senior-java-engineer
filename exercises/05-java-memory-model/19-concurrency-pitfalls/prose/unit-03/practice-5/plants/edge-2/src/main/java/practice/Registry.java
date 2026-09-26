package practice;

import java.util.concurrent.atomic.AtomicInteger;

public final class Registry {

    private static final AtomicInteger CREATED = new AtomicInteger();

    private Registry() {
        CREATED.incrementAndGet();
    }

    private static Registry instance;

    /** The one Registry, created lazily on first use. */
    public static synchronized Registry getInstance() {
        if (instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    /** How many Registry objects were ever constructed. */
    public static int instancesCreated() {
        return CREATED.get();
    }
}
