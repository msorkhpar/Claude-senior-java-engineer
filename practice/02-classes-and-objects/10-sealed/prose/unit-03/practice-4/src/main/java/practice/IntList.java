package practice;

public class IntList {

    public sealed interface Node permits Empty, Cons {
    }

    public record Empty() implements Node {
    }

    public record Cons(int head, Node tail) implements Node {
    }

    public static Node of(int... values) {
        throw new UnsupportedOperationException("write of");
    }

    public static int sum(Node list) {
        throw new UnsupportedOperationException("write sum");
    }

    public static Node append(Node list, int value) {
        throw new UnsupportedOperationException("write append");
    }

    public static Node prepend(Node list, int value) {
        throw new UnsupportedOperationException("write prepend");
    }
}
