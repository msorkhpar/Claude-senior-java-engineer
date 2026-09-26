package practice;

import java.util.List;
import java.util.function.Predicate;

public final class LengthRule {

    private final int min;

    public LengthRule(int min) {
        this.min = min;
    }

    /** Whether the word is at least min characters long; null is never accepted. */
    public boolean accepts(String word) {
        return word.length() >= min;
    }

    /** This rule's accepts, as a predicate. */
    public Predicate<String> asPredicate() {
        return this::accepts;
    }

    /** The accepted words, in order. */
    public List<String> keep(List<String> words) {
        return words.stream().filter(this::accepts).toList();
    }
}
