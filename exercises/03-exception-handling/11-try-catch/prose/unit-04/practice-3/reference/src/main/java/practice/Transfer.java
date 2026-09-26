package practice;

import java.io.IOException;

public final class Transfer {

    private Transfer() {
    }

    /** A readable, writable channel. */
    public interface Channel extends AutoCloseable {
        String read() throws IOException;

        void write(String text) throws IOException;

        @Override
        void close() throws IOException;
    }

    /** Opens a channel by name. */
    public interface Opener {
        Channel open(String name) throws IOException;
    }

    /** Opens "in" then "out", writes to out what in reads, and closes both. */
    public static void copy(Opener opener) throws IOException {
        try (Channel in = opener.open("in");
             Channel out = opener.open("out")) {
            out.write(in.read());
        }
    }
}
