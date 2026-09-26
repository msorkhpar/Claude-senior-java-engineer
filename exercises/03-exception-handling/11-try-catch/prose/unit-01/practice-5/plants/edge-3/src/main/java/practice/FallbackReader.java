package practice;

import java.io.IOException;

public final class FallbackReader {

    private FallbackReader() {
    }

    /** Reads a named document. */
    public interface Source {
        String read(String name) throws IOException;
    }

    /** Reads {@code name} from {@code primary}, falling back to {@code backup} on an IOException. */
    public static String read(Source primary, Source backup, String name) throws IOException {
        try {
            return primary.read(name);
        } catch (IOException primaryFailure) {
            try {
                return backup.read(name);
            } catch (IOException backupFailure) {
                primaryFailure.addSuppressed(backupFailure);
                throw primaryFailure;
            }
        }
    }
}
