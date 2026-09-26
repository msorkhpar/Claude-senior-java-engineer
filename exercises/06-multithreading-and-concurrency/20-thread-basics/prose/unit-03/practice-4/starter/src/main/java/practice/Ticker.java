package practice;

public final class Ticker implements Runnable {

    public Ticker(Runnable tick, long pauseMillis, Runnable cleanup) {
    }

    /** Ticks, pausing between ticks, until cancelled or interrupted; then runs the cleanup once. */
    @Override
    public void run() {
        throw new UnsupportedOperationException("write run");
    }

    /** Stops the ticker from any thread. */
    public void cancel() {
        throw new UnsupportedOperationException("write cancel");
    }
}
