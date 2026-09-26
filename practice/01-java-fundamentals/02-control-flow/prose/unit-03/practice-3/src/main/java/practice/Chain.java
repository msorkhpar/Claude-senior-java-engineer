package practice;

public final class Chain {

    /** One link of a singly linked list: a value and the next link, or null at the end. */
    public record Node(int value, Node next) {
    }

    private Chain() {
    }

    /** Returns the position of the first link holding target, or -1. */
    public static int indexOf(Node head, int target) {
        throw new UnsupportedOperationException("write indexOf");
    }
}
