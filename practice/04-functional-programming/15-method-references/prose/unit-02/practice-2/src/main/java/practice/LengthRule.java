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
        throw new UnsupportedOperationException("write accepts");
    }

    /** This rule's accepts, as a predicate. */
    public Predicate<String> asPredicate() {
        throw new UnsupportedOperationException("write asPredicate");
    }

    /** The accepted words, in order. */
    public List<String> keep(List<String> words) {
        throw new UnsupportedOperationException("write keep");
    }
}
