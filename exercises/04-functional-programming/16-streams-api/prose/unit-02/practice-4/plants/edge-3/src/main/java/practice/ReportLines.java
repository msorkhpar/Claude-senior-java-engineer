package practice;

import java.util.List;
import java.util.stream.Stream;

public final class ReportLines {

    private ReportLines() {
    }

    /** Returns HEADER (if asked), the trimmed non-blank items, then FOOTER (if asked). */
    public static List<String> lines(List<String> items, boolean header, boolean footer) {
        Stream.Builder<String> builder = Stream.builder();
        if (header) {
            builder.add("HEADER");
        }
        for (String item : items) {
            if (!item.isBlank()) {
                builder.add(item.trim());
            }
        }
        if (footer) {
            builder.add("FOOTER");
        }
        return builder.build().toList();
    }
}
