package practice;

public final class Ticker implements Runnable {

    private final Runnable tick;
    private final long pauseMillis;
    private final Runnable cleanup;
    private volatile boolean cancelled;
    private volatile Thread runner;

    public Ticker(Runnable tick, long pauseMillis, Runnable cleanup) {
        this.tick = tick;
        this.pauseMillis = pauseMillis;
        this.cleanup = cleanup;
    }

    /** Ticks, pausing between ticks, until cancelled or interrupted; then runs the cleanup once. */
    @Override
    public void run() {
        runner = Thread.currentThread();
        try {
            while (!cancelled) {
                tick.run();
                try {
                    Thread.sleep(pauseMillis);
                } catch (InterruptedException e) {
                    // woken early: go round again and look at the flag
                }
            }
        } finally {
            cleanup.run();
        }
    }

    /** Stops the ticker from any thread. */
    public void cancel() {
        cancelled = true;
        Thread running = runner;
        if (running != null) {
            running.interrupt();
        }
    }
}
