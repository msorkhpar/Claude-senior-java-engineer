package practice;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class Tasks {

    private Tasks() {
    }

    /** Returns a Supplier that calls {@code task} on every get(), rethrowing checked failures unchecked. */
    public static <T> Supplier<T> unchecked(Callable<T> task) {
        throw new UnsupportedOperationException("write unchecked");
    }
}
