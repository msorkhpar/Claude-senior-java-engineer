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
        // TODO: Jobs of different sizes share these sessions, and a 4K job that is waiting must
        //       not be passed over again and again by 1080p jobs that arrive after it.
        throw new IllegalStateException("TODO: newSessions");
    }

    /** Waits until {@code n} sessions are free, and takes them for one job. */
    public void reserve(int n) throws InterruptedException {
        // TODO: Return once the job has its n sessions. While it waits, it must not keep sessions
        //       other jobs could use, and a job cancelled while waiting leaves at once, holding none.
        throw new IllegalStateException("TODO: reserve");
    }

    /** Takes {@code n} sessions if the job can have them right now; false otherwise. */
    public boolean tryReserve(int n) {
        // TODO: The job either gets all n sessions now, or none of them.
        throw new IllegalStateException("TODO: tryReserve");
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
