package practice;

public final class Keeper {

    private Keeper() {
    }

    /** Returns the thread running {@code job}: starts {@code thread} if it is new, replaces it if it has finished. */
    public static Thread ensureStarted(Thread thread, Runnable job) {
        if (thread.getState() == Thread.State.TERMINATED) {
            Thread replacement = new Thread(job, thread.getName());
            replacement.start();
            return replacement;
        }
        thread.start();
        return thread;
    }
}
