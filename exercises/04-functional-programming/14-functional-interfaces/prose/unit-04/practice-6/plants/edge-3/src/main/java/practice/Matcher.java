package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public final class Matcher {
    private Matcher() {
    }

    public static BiPredicate<String, String> startsWithAndLongerThan(int minLength) {
        BiPredicate<String, String> longEnough = (word, prefix) -> word.length() > minLength;
        BiPredicate<String, String> startsWith = String::startsWith;
        return longEnough.and(startsWith);
    }

    public static List<String> matching(List<String> words, String prefix, BiPredicate<String, String> rule) {
        List<String> out = new ArrayList<>();
        for (String w : words) {
            try {
                if (rule.test(w, prefix)) {
                    out.add(w);
                }
            } catch (NullPointerException e) {
                // a null word cannot match
            }
        }
        return out;
    }
}
