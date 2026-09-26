package practice;

import java.io.IOException;

public final class FallbackReader {

    private FallbackReader() {
    }

    public interface Source {
        String read(String name) throws IOException;
    }

    public static String read(Source primary, Source backup, String name) throws IOException {
        IOException primaryFailure;
        try {
            return primary.read(name);
        } catch (IOException first) {
            try {
                return primary.read(name);
            } catch (IOException second) {
                primaryFailure = second;
            }
        }
        try {
            return backup.read(name);
        } catch (IOException backupFailure) {
            backupFailure.addSuppressed(primaryFailure);
            throw backupFailure;
        }
    }
}
