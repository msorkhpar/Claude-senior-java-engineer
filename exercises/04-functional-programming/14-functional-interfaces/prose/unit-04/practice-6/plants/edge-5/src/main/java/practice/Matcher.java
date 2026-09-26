package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class Matcher {
    private Matcher() {
    }

    public static BiPredicate<String, String> startsWithAndLongerThan(int minLength) {
        BiPredicate<String, String> longEnough = (word, prefix) -> word.length() > minLength;
        BiPredicate<String, String> startsWith = (word, prefix) -> word.toLowerCase().startsWith(prefix.toLowerCase());
        return longEnough.and(startsWith);
    }

    public static List<String> matching(List<String> words, String prefix, BiPredicate<String, String> rule) {
        return words.stream().filter(Objects::nonNull).filter(w -> rule.test(w, prefix)).toList();
    }
}
