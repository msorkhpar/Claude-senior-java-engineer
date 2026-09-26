package practice;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class Rendezvous {
    private final int workers;
    private final Semaphore gate;

    public Rendezvous(int workers) { this.workers = workers; this.gate = new Semaphore(1 - workers); }

    public void ready() { gate.release(); }

    public boolean awaitAll(long timeout, TimeUnit unit) throws InterruptedException {
        return gate.tryAcquire(unit.toSeconds(timeout), TimeUnit.SECONDS);
    }
}
