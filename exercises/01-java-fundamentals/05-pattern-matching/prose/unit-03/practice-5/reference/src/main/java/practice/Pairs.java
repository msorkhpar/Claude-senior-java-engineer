package practice;

public final class Pairs {

    /** Two values of any types. */
    public record Pair<A, B>(A first, B second) {
    }

    private Pairs() {
    }

    /** Labels a pair by the run-time types of its values. */
    public static String label(Object obj) {
        if (obj instanceof Pair<?, ?>(String name, Integer age)) {
            return name + " is " + age;
        } else if (obj instanceof Pair<?, ?>(Integer a, Integer b)) {
            return "sum " + (a + b);
        } else if (obj instanceof Pair<?, ?>) {
            return "pair";
        }
        return "not a pair";
    }
}
