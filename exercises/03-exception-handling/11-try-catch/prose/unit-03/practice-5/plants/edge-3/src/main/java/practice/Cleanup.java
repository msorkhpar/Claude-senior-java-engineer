package practice;

public final class Cleanup {

    private Cleanup() {
    }

    /** Runs {@code work}, then always {@code cleanup}; a failing cleanup keeps the work's failure as its cause. */
    public static void run(Runnable work, Runnable cleanup) {
        RuntimeException workFailure = null;
        try {
            work.run();
        } catch (RuntimeException e) {
            workFailure = e;
            throw e;
        } finally {
            try {
                cleanup.run();
            } catch (RuntimeException cleanupFailure) {
                if (workFailure != null) {
                    cleanupFailure.addSuppressed(workFailure);
                }
                throw cleanupFailure;
            }
        }
    }
}
