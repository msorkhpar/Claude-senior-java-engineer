package practice;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Lets a coordinator wait until every worker has called {@link #ready()}. */
public final class Rendezvous {

    private final Semaphore gate;

    public Rendezvous(int workers) {
        this.gate = new Semaphore(0);
    }

    /** Reports one worker ready; any thread may call it, at any time. */
    public void ready() {
        gate.release();
    }

    /** Waits at most {@code timeout} for every worker to be ready; returns whether they all were. */
    public boolean awaitAll(long timeout, TimeUnit unit) throws InterruptedException {
        return gate.tryAcquire(timeout, unit);
    }
}
