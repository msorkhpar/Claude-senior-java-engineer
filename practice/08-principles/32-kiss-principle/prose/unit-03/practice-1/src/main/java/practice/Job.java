package practice;

import java.util.concurrent.atomic.AtomicReference;

public class Job {

    /** The states a job moves through. */
    public enum State {
        IDLE, RUNNING, COMPLETED, FAILED
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);

    /** Moves IDLE to RUNNING. */
    public boolean start() {
        throw new UnsupportedOperationException("write start");
    }

    /** Moves RUNNING to COMPLETED. */
    public boolean complete() {
        throw new UnsupportedOperationException("write complete");
    }

    /** Moves RUNNING to FAILED. */
    public boolean fail() {
        throw new UnsupportedOperationException("write fail");
    }

    /** Moves COMPLETED or FAILED back to IDLE. */
    public boolean reset() {
        throw new UnsupportedOperationException("write reset");
    }

    /** Returns the current state. */
    public State state() {
        throw new UnsupportedOperationException("write state");
    }
}
