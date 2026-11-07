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
        // TODO: turn the feed's bytes into lines of text. A feed whose bytes aren't valid in the
        //       declared charset must end the read with an IOException, before its bad line is handed on.
        throw new IllegalStateException("TODO: forEachLine");
    }
}
