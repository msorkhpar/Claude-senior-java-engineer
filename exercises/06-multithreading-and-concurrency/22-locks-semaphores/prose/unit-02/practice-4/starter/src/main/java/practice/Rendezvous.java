package practice;

import java.util.concurrent.TimeUnit;

/** Lets a coordinator wait until every worker has called {@link #ready()}. */
public final class Rendezvous {

    public Rendezvous(int workers) {
    }

    /** Reports one worker ready; any thread may call it, at any time. */
    public void ready() {
        throw new UnsupportedOperationException("write ready");
    }

    /** Waits at most {@code timeout} for every worker to be ready; returns whether they all were. */
    public boolean awaitAll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("write awaitAll");
    }
}
