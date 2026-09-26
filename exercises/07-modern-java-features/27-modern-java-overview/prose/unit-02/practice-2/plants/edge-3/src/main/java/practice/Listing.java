package practice;

import java.util.ArrayList;
import java.util.List;

public final class Listing {

    private Listing() {
    }

    /** Each line as "<n>: <line>", numbers right-aligned to the widest one. */
    public static List<String> number(String text) {
        List<String> lines = List.of(text.split("\\R"));
        int width = String.valueOf(lines.size()).length();
        List<String> out = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String n = String.valueOf(i + 1);
            out.add(" ".repeat(width - n.length()) + n + ": " + lines.get(i));
        }
        return out;
    }
}
