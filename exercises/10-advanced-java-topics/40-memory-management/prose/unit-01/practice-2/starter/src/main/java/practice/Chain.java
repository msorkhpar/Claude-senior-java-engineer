package practice;

public final class Chain {

    /** One link of a chain; null ends it. */
    public record Node(int value, Node next) {
    }

    private Chain() {
    }

    /** The sum of all values in the chain starting at head. */
    public static long sum(Node head) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The number of nodes in the chain starting at head. */
    public static int length(Node head) {
        throw new UnsupportedOperationException("TODO");
    }

    /** n! for n >= 0. */
    public static long factorial(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
