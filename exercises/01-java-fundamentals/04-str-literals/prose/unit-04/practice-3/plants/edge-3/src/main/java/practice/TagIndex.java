package practice;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Counts how many posts carry each tag, for the blog's tag cloud and its tag search. The cloud
 * shows each tag as it was first typed, so the index keeps the text and compares Tags.
 */
public class TagIndex {

    /** A tag as an author typed it. Tags are keys of the index's map. */
    public static final class Tag {
        private final String text;

        public Tag(String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Tag t && key(text).equalsIgnoreCase(key(t.text));
        }

        @Override
        public int hashCode() {
            return key(text).toLowerCase(Locale.ROOT).hashCode();
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private final Map<Tag, Integer> posts = new LinkedHashMap<>();

    /** Records one post tagged {@code tag}. */
    public void add(String tag) {
        posts.merge(new Tag(tag), 1, Integer::sum);
    }

    /** Each distinct tag, as first typed, with its number of posts, in the order tags first appeared. */
    public Map<String, Integer> counts() {
        Map<String, Integer> shown = new LinkedHashMap<>();
        posts.forEach((tag, n) -> shown.put(tag.text(), n));
        return shown;
    }

    /** How many posts carry the tag a reader searched for; 0 if none. */
    public int postsTagged(String query) {
        return posts.getOrDefault(new Tag(query), 0);
    }

    /** One form for every way a device can send the same accented letters. */
    static String key(String tag) {
        return Normalizer.normalize(tag, Normalizer.Form.NFC);
    }
}
