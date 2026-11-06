package practice;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Sends payments to the fraud-scoring vendor. The vendor's contract allows this client a fixed
 * number of calls in progress at once; a call that can't get one of those sessions in time is
 * refused with {@link Busy}, and the checkout falls back to its offline rules.
 */
public class ScoringClient {

    /** One call to the vendor; returns the risk score, or throws when the call fails. */
    public interface Vendor {
        int score(String payment) throws Exception;
    }

    /** No session came free in time. */
    public static class Busy extends RuntimeException {
        public Busy(String payment) {
            super("no scoring session free for " + payment);
        }
    }

    private final Vendor vendor;
    private final Semaphore sessions;
    private final long waitMillis;

    public ScoringClient(Vendor vendor, int maxSessions, long waitMillis) {
        this.vendor = vendor;
        this.sessions = new Semaphore(maxSessions);
        this.waitMillis = waitMillis;
    }

    /** Scores one payment, using one of the vendor's sessions for the length of the call. */
    public int score(String payment) throws Exception {
        // TODO: Call the vendor with one of its sessions. Wait at most waitMillis for a session to
        //       come free, and throw Busy if none does. However the call ends (a score, a vendor
        //       failure, a refusal, or the request being cancelled while it waits), the number of
        //       sessions this client can use must stay what the contract allows.
        throw new IllegalStateException("TODO: score");
    }

    /** Sessions free right now, for the client's gauge. */
    int freeSessions() {
        return sessions.availablePermits();
    }

    /** Callers waiting for a session right now, for the client's gauge. */
    int waitingCallers() {
        return sessions.getQueueLength();
    }
}
