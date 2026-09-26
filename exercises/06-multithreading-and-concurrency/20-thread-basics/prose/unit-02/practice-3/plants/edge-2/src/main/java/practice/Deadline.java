package practice;

public final class Deadline {

    /** How a bounded wait for a worker ended. */
    public enum Outcome { FINISHED, CANCELLED, NOT_STARTED }

    private Deadline() {
    }

    /** Waits at most millis for worker; interrupts it if it is still running then. */
    public static Outcome await(Thread worker, long millis) throws InterruptedException {
        if (millis <= 0) {
            throw new IllegalArgumentException("a timeout must be positive, and join(0) waits forever");
        }
        worker.join(millis);
        if (worker.isAlive()) {
            worker.interrupt();
            return Outcome.CANCELLED;
        }
        return Outcome.FINISHED;
    }
}
