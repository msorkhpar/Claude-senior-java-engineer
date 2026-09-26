package practice;

public final class Chain {

    /** One link of a chain; null ends it. */
    public record Node(int value, Node next) {
    }

    private Chain() {
    }

    /** The sum of all values in the chain starting at head. */
    public static long sum(Node head) {
        long total = 0;
        for (Node n = head; n != null; n = n.next()) {
            total += n.value();
        }
        return total;
    }

    /** The number of nodes in the chain starting at head. */
    public static int length(Node head) {
        int count = 0;
        for (Node n = head; n != null; n = n.next()) {
            count++;
        }
        return count;
    }

    /** n! for n >= 0. */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("negative input: " + n);
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
