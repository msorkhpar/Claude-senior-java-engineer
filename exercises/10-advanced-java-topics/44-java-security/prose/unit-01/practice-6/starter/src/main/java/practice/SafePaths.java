package practice;

import java.nio.file.Path;

public final class SafePaths {

    private SafePaths() {
    }

    /** Returns {@code userSupplied} resolved inside {@code base}, or throws SecurityException. */
    public static Path resolve(Path base, String userSupplied) {
        throw new UnsupportedOperationException("TODO");
    }
}
