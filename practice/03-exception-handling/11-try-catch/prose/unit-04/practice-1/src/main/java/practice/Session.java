package practice;

import java.util.List;

public final class Session implements AutoCloseable {

    /** Opens a session named {@code name} that records what happens in {@code log}. */
    public Session(String name, List<String> log) {
        throw new UnsupportedOperationException("write Session");
    }

    /** Records "use <what>". */
    public void use(String what) {
        throw new UnsupportedOperationException("write use");
    }

    /** Records "close <name>". */
    @Override
    public void close() {
        throw new UnsupportedOperationException("write close");
    }

    /** Returns whether the session has been closed. */
    public boolean isClosed() {
        throw new UnsupportedOperationException("write isClosed");
    }
}
