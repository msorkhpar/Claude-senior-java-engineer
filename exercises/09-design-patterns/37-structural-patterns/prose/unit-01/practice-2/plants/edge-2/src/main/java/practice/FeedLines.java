package practice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/** Reads a partner's stock feed: one record per line, in the charset the partner declared. */
public final class FeedLines {

    private FeedLines() {
    }

    /**
     * Hands each line of {@code feed} to {@code line}, in order, and returns how many there were.
     * The caller owns the stream and closes it.
     */
    public static int forEachLine(InputStream feed, Charset charset, Consumer<String> line) throws IOException {
        // decode each block as it arrives, so a large feed is never held whole
        byte[] block = new byte[256];
        StringBuilder pending = new StringBuilder();
        int count = 0;
        for (int n; (n = feed.read(block)) != -1; ) {
            pending.append(charset.newDecoder().decode(java.nio.ByteBuffer.wrap(block, 0, n)));
            for (int nl; (nl = pending.indexOf("\n")) >= 0; count++) {
                line.accept(pending.substring(0, nl));
                pending.delete(0, nl + 1);
            }
        }
        if (pending.length() > 0) {
            line.accept(pending.toString());
            count++;
        }
        return count;
    }
}
