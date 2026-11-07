package practice;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Issue labels, as people type them: " Bug", "bug ", "BUG" are one label. */
public class Labels {

    /** One label. Its name is trimmed and in lower case, however it was typed. */
    public record Label(String name) {
        public Label {
            name = normalize(name);
        }
    }

    /** The trimmed, lower-case form of a typed label. */
    static String normalize(String typed) {
        return typed.strip().toLowerCase(Locale.ROOT);
    }

    /** The distinct labels among what people typed, in the order they first appear. */
    public static Set<Label> distinct(List<String> typed) {
        Set<Label> labels = new LinkedHashSet<>();
        for (String t : typed) {
            labels.add(new Label(t));
        }
        return labels;
    }
}
