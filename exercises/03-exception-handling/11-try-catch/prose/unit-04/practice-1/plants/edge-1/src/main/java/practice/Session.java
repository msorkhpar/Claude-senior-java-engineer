package practice;

import java.util.List;

public final class Session implements AutoCloseable {

    private final String name;
    private final List<String> log;
    private boolean closed;

    /** Opens a session named {@code name} that records what happens in {@code log}. */
    public Session(String name, List<String> log) {
        this.name = name;
        this.log = log;
    }

    /** Records "use <what>". */
    public void use(String what) {
        log.add("use " + what);
    }

    /** Records "close <name>". */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        log.add("close " + name);
    }

    /** Returns whether the session has been closed. */
    public boolean isClosed() {
        return closed;
    }
}
