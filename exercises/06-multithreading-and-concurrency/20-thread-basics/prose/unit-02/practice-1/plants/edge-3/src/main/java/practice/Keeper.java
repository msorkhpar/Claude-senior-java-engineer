package practice;

public final class Keeper {

    private Keeper() {
    }

    /** Returns the thread running {@code job}: starts {@code thread} if it is new, replaces it if it has finished. */
    public static Thread ensureStarted(Thread thread, Runnable job) {
        switch (thread.getState()) {
            case NEW -> {
                thread.start();
                return thread;
            }
            case TERMINATED -> {
                Thread replacement = new Thread(job);
                replacement.start();
                return replacement;
            }
            default -> {
                return thread;
            }
        }
    }
}
