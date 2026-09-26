package practice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SafePaths {

    private SafePaths() {
    }

    /** Returns {@code userSupplied} resolved inside {@code base}, or throws SecurityException. */
    public static Path resolve(Path base, String userSupplied) {
        Path root = base.toAbsolutePath().normalize();
        Path resolved = root.resolve(userSupplied).normalize();
        if (!resolved.startsWith(root)) {
            throw new SecurityException("path traversal: " + userSupplied);
        }
        if (Files.exists(resolved)) {
            try {
                if (!resolved.toRealPath().startsWith(root.toRealPath())) {
                    throw new SecurityException("path traversal through a link: " + userSupplied);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return resolved;
    }
}
