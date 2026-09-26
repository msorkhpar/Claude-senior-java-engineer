package practice;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class Outcomes {

    private Outcomes() {
    }

    /** Waits at most timeout for the future and describes how its task ended. */
    public static String describe(Future<?> future, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("write describe");
    }
}
