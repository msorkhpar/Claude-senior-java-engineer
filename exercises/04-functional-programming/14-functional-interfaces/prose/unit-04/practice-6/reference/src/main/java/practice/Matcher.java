package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class Matcher {

    private Matcher() {
    }

    /** Returns a rule true when the word is longer than minLength and starts with the prefix. */
    public static BiPredicate<String, String> startsWithAndLongerThan(int minLength) {
        BiPredicate<String, String> longEnough = (word, prefix) -> word.length() > minLength;
        BiPredicate<String, String> startsWith = String::startsWith;
        return longEnough.and(startsWith);
    }

    /** Returns the non-null words for which rule.test(word, prefix) holds, in order. */
    public static List<String> matching(List<String> words, String prefix, BiPredicate<String, String> rule) {
        return words.stream()
                .filter(Objects::nonNull)
                .filter(word -> rule.test(word, prefix))
                .toList();
    }
}
