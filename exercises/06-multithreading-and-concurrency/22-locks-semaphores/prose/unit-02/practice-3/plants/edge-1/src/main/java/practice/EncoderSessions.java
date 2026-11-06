package practice;

import java.util.concurrent.Semaphore;

/**
 * Hands out the GPU's hardware encoder sessions to transcoding jobs. The GPU runs a fixed number
 * of sessions at once; a 1080p job uses one, a 4K job several.
 */
public class EncoderSessions {

    private final Semaphore sessions;

    public EncoderSessions(int sessions) {
        this.sessions = newSessions(sessions);
    }

    /** Creates the semaphore whose permits are the GPU's sessions. */
    Semaphore newSessions(int count) {
        return new Semaphore(count, true);
    }

    /** Waits until {@code n} sessions are free, and takes them for one job. */
    public void reserve(int n) throws InterruptedException {
        for (int i = 0; i < n; i++) sessions.acquire();
    }

    /** Takes {@code n} sessions if the job can have them right now; false otherwise. */
    public boolean tryReserve(int n) {
        return sessions.tryAcquire(n);
    }

    /** Returns the {@code n} sessions a finished job reserved. */
    public void giveBack(int n) {
        sessions.release(n);
    }

    /** Sessions free right now, for the transcoder's gauge. */
    public int free() {
        return sessions.availablePermits();
    }

    /** Jobs waiting for sessions right now, for the transcoder's gauge. */
    int waitingJobs() {
        return sessions.getQueueLength();
    }
}
