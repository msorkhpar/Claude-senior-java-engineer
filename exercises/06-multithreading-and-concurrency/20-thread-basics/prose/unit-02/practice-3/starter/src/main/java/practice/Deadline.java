package practice;

public final class Deadline {

    /** How a bounded wait for a worker ended. */
    public enum Outcome { FINISHED, CANCELLED, NOT_STARTED }

    private Deadline() {
    }

    /** Waits at most millis for worker; interrupts it if it is still running then. */
    public static Outcome await(Thread worker, long millis) throws InterruptedException {
        throw new UnsupportedOperationException("write await");
    }
}
