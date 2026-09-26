package practice;

public final class Cleanup {

    private Cleanup() {
    }

    /** Runs {@code work}, then always {@code cleanup}; a failing cleanup keeps the work's failure as its cause. */
    public static void run(Runnable work, Runnable cleanup) {
        try {
            work.run();
        } finally {
            cleanup.run();
        }
    }
}
