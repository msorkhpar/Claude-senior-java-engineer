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
        try (Resource resource = opener.open("r")) {
            body.run();
            log.add("body done");
        } catch (Exception e) {
            log.add("caught " + e.getMessage());
        } finally {
            log.add("finally");
        }
    }
}
