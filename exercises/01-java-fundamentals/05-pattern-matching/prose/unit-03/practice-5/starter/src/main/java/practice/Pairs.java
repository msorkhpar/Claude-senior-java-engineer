package practice;

public final class Pairs {

    /** Two values of any types. */
    public record Pair<A, B>(A first, B second) {
    }

    private Pairs() {
    }

    /** Labels a pair by the run-time types of its values. */
    public static String label(Object obj) {
        throw new UnsupportedOperationException("write label");
    }
}
