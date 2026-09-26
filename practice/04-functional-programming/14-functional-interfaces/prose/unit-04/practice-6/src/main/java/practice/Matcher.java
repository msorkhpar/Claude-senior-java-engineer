package practice;

import java.util.List;
import java.util.function.BiPredicate;

public final class Matcher {

    private Matcher() {
    }

    /** Returns a rule true when the word is longer than minLength and starts with the prefix. */
    public static BiPredicate<String, String> startsWithAndLongerThan(int minLength) {
        throw new UnsupportedOperationException("write startsWithAndLongerThan");
    }

    /** Returns the non-null words for which rule.test(word, prefix) holds, in order. */
    public static List<String> matching(List<String> words, String prefix, BiPredicate<String, String> rule) {
        throw new UnsupportedOperationException("write matching");
    }
}
