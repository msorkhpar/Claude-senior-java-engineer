package practice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public final class Traced {

    private Traced() {
    }

    public interface Resource extends AutoCloseable {
        @Override
        void close() throws IOException;
    }

    public interface Opener {
        Resource open(String name) throws IOException;
    }

    public interface Body {
        void run() throws IOException;
    }

    public static void run(Opener opener, Body body, List<String> log) {
        Resource resource;
        try {
            resource = opener.open("r");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (resource) {
            body.run();
            log.add("body done");
        } catch (IOException e) {
            log.add("caught " + e.getMessage());
        } finally {
            log.add("finally");
        }
    }
}
