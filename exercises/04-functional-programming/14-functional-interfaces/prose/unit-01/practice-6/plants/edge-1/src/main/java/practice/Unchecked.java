package practice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public final class Unchecked {

    private Unchecked() {
    }

    /** A consumer whose accept may throw IOException. */
    @FunctionalInterface
    public interface IoAction<T> {
        void accept(T t) throws IOException;
    }

    /** Returns a Consumer that runs {@code action}, rethrowing an IOException as UncheckedIOException. */
    public static <T> Consumer<T> unchecked(IoAction<T> action) {
        return t -> {
            try {
                action.accept(t);
            } catch (IOException e) {
                throw new UncheckedIOException(new IOException(e.getMessage()));
            }
        };
    }
}
