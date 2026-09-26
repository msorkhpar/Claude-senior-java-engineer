package practice;

import java.util.function.BooleanSupplier;

public final class Poller {

    private Poller() {
    }

    /**
     * Polls {@code condition}, sleeping {@code pollMillis} between checks, and returns true once it holds;
     * returns false, with the interrupt flag set, if the thread is interrupted.
     */
    public static boolean waitUntil(BooleanSupplier condition, long pollMillis) {
        while (!condition.getAsBoolean()) {
            try {
                Thread.sleep(pollMillis);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }
}
