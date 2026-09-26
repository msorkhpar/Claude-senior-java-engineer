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
        return state.compareAndSet(State.IDLE, State.RUNNING);
    }

    /** Moves RUNNING to COMPLETED. */
    public boolean complete() {
        return state.compareAndSet(State.RUNNING, State.COMPLETED);
    }

    /** Moves RUNNING to FAILED. */
    public boolean fail() {
        return state.compareAndSet(State.RUNNING, State.FAILED);
    }

    /** Moves COMPLETED or FAILED back to IDLE. */
    public boolean reset() {
        state.set(State.IDLE);
        return true;
    }

    /** Returns the current state. */
    public State state() {
        return state.get();
    }
}
