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
        state.set(State.RUNNING);
        return true;
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
        State current = state.get();
        if (current == State.COMPLETED || current == State.FAILED) {
            return state.compareAndSet(current, State.IDLE);
        }
        return false;
    }

    /** Returns the current state. */
    public State state() {
        return state.get();
    }
}
