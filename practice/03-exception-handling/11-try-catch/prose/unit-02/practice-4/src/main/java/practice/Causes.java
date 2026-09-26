package practice;

import java.util.Optional;

public final class Causes {

    private Causes() {
    }

    /** Returns the first throwable in the cause chain starting at {@code thrown} that is a {@code type}. */
    public static <T extends Throwable> Optional<T> find(Throwable thrown, Class<T> type) {
        throw new UnsupportedOperationException("write find");
    }
}
