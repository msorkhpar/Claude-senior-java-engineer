package practice;

import java.io.IOException;
import java.io.InputStream;

public final class BoundedReader {

    private BoundedReader() {
    }

    /** Reads {@code in} as UTF-8 text, refusing more than {@code limit} bytes; always closes {@code in}. */
    public static String read(InputStream in, int limit) throws IOException {
        throw new UnsupportedOperationException("TODO");
    }
}
