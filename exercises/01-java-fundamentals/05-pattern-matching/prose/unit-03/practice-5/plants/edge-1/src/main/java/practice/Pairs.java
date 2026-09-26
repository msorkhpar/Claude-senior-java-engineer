package practice;

public final class Pairs {

    /** Two values of any types. */
    public record Pair<A, B>(A first, B second) {
    }

    private Pairs() {
    }

    /** Labels a pair by the run-time types of its values. */
    public static String label(Object obj) {
        if (obj instanceof Pair<?, ?> p) {
            if (p.first() instanceof Integer a && p.second() instanceof Integer b) {
                return "sum " + (a + b);
            }
            if (p.second() instanceof Integer age) {
                return p.first() + " is " + age;
            }
            return "pair";
        }
        return "not a pair";
    }
}
