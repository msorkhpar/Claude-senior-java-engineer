package practice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class Tasks {

    private Tasks() {
    }

    /** Returns a Supplier that calls {@code task} on every get(), rethrowing checked failures unchecked. */
    public static <T> Supplier<T> unchecked(Callable<T> task) {
        return () -> {
            try {
                return task.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        };
    }
}
