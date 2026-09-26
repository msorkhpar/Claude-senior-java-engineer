package practice;

import java.io.IOException;

public final class Transfer {

    private Transfer() {
    }

    public interface Channel extends AutoCloseable {
        String read() throws IOException;

        void write(String text) throws IOException;

        @Override
        void close() throws IOException;
    }

    public interface Opener {
        Channel open(String name) throws IOException;
    }

    public static void copy(Opener opener) throws IOException {
        Channel in = opener.open("in");
        Channel out = null;
        try {
            out = opener.open("out");
            out.write(in.read());
        } finally {
            if (out != null) {
                out.close();
            }
            in.close();
        }
    }
}
