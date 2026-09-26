package practice;

import java.util.concurrent.atomic.AtomicReference;

public final class Crash {

    private Crash() {
    }

    /** Runs task on a new thread named name, waits for it, and returns "name: message" of what it threw, or null. */
    public static String runAndReport(String name, Runnable task) throws InterruptedException {
        throw new UnsupportedOperationException("write runAndReport");
    }
}
