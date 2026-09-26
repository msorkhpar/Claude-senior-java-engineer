package practice;

import java.util.function.IntSupplier;

public final class Meter {

    /** Runs {@code task}, counting it in flight while it runs, and returns its value. */
    public int call(IntSupplier task) {
        throw new UnsupportedOperationException("write call");
    }

    /** Returns how many calls are running right now. */
    public int inFlight() {
        throw new UnsupportedOperationException("write inFlight");
    }
}
