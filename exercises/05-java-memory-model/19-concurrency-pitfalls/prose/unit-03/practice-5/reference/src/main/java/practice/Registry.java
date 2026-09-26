package practice;

import java.util.concurrent.atomic.AtomicInteger;

public final class Registry {

    private static final AtomicInteger CREATED = new AtomicInteger();

    private Registry() {
        CREATED.incrementAndGet();
    }

    private static final class Holder {
        static final Registry INSTANCE = new Registry();
    }

    /** The one Registry, created lazily on first use. */
    public static Registry getInstance() {
        return Holder.INSTANCE;
    }

    /** How many Registry objects were ever constructed. */
    public static int instancesCreated() {
        return CREATED.get();
    }
}
