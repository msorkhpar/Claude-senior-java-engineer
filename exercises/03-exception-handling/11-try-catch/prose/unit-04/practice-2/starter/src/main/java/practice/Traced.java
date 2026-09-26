package practice;

import java.io.IOException;
import java.util.List;

public final class Traced {

    private Traced() {
    }

    /** A resource that may fail to close. */
    public interface Resource extends AutoCloseable {
        @Override
        void close() throws IOException;
    }

    /** Opens a resource by name. */
    public interface Opener {
        Resource open(String name) throws IOException;
    }

    /** The work done while the resource is open. */
    public interface Body {
        void run() throws IOException;
    }

    /** Runs {@code body} with the resource "r" open, recording the steps in {@code log}. */
    public static void run(Opener opener, Body body, List<String> log) {
        throw new UnsupportedOperationException("write run");
    }
}
