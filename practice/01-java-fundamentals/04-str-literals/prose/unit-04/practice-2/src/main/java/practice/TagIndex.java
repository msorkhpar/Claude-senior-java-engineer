package practice;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Counts how many posts carry each tag, for the blog's tag cloud and its tag search. Authors type
 * tags on phones, laptops and tablets, and readers search from any of them.
 */
public class TagIndex {

    private final Map<String, Integer> posts = new LinkedHashMap<>();

    /** Records one post tagged {@code tag}. */
    public void add(String tag) {
        posts.merge(key(tag), 1, Integer::sum);
    }

    /** Each distinct tag with its number of posts, in the order the tags first appeared. */
    public Map<String, Integer> counts() {
        return new LinkedHashMap<>(posts);
    }

    /** How many posts carry the tag a reader searched for; 0 if none. */
    public int postsTagged(String query) {
        // TODO: Look the query up the way the tags were stored.
        throw new IllegalStateException("TODO: postsTagged");
    }

    /** The form in which a tag is stored and looked up. */
    static String key(String tag) {
        // TODO: Return the form a tag is stored and looked up in. The same tag typed on two
        //       devices must end up as one entry, and two different tags as two.
        throw new IllegalStateException("TODO: key");
    }
}
