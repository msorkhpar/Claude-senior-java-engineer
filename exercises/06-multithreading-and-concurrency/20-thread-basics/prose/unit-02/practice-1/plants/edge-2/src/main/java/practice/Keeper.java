package practice;

public final class Keeper {

    private Keeper() {
    }

    /** Returns the thread running {@code job}: starts {@code thread} if it is new, replaces it if it has finished. */
    public static Thread ensureStarted(Thread thread, Runnable job) {
        if (thread.isAlive()) {
            return thread;
        }
        thread.start();
        return thread;
    }
}
