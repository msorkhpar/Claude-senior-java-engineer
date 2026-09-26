package practice;

import java.util.function.IntSupplier;

public final class Meter {

    private int inFlight;

    /** Runs {@code task}, counting it in flight while it runs, and returns its value. */
    public int call(IntSupplier task) {
        inFlight++;
        int result = -1;
        try {
            result = task.getAsInt();
        } finally {
            inFlight--;
            return result;
        }
    }

    /** Returns how many calls are running right now. */
    public int inFlight() {
        return inFlight;
    }
}
