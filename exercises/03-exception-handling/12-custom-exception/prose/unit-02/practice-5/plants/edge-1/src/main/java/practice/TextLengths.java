package practice;

import java.io.IOException;
import java.util.List;

/** Fetches a text by name. */
interface TextSource {
    String fetch(String name) throws IOException;
}

public final class TextLengths {

    private TextLengths() {
    }

    /** The length of each fetched text, in order; a failed fetch throws UncheckedIOException("Could not read " + name). */
    public static List<Integer> lengths(List<String> names, TextSource source) {
        return names.stream()
                .map(name -> {
                    try {
                        return source.fetch(name).length();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not read " + name);
                    }
                })
                .toList();
    }
}
