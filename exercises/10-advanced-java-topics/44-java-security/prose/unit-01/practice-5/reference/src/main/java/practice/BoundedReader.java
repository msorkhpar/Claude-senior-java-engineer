package practice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class BoundedReader {

    private BoundedReader() {
    }

    /** Reads {@code in} as UTF-8 text, refusing more than {@code limit} bytes; always closes {@code in}. */
    public static String read(InputStream in, int limit) throws IOException {
        try (in) {
            byte[] data = in.readNBytes(limit + 1);
            if (data.length > limit) {
                throw new SecurityException("upload larger than " + limit + " bytes");
            }
            return new String(data, StandardCharsets.UTF_8);
        }
    }
}
