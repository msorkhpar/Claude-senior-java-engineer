package practice;

public class IntList {

    public sealed interface Node permits Empty, Cons {
    }

    public record Empty() implements Node {
    }

    public record Cons(int head, Node tail) implements Node {
    }

    public static Node of(int... values) {
        Node list = new Empty();
        for (int i = values.length - 1; i >= 0; i--) {
            list = new Cons(values[i], list);
        }
        return list;
    }

    public static int sum(Node list) {
        return switch (list) {
            case Empty e -> 0;
            case Cons(int head, Node tail) -> head + sum(tail);
        };
    }

    public static Node append(Node list, int value) {
        return switch (list) {
            case Empty e -> new Cons(value, e);
            case Cons(int head, Node tail) -> new Cons(head, append(tail, value));
        };
    }

    public static Node prepend(Node list, int value) {
        return new Cons(value, copy(list));
    }

    private static Node copy(Node list) {
        return switch (list) {
            case Empty e -> new Empty();
            case Cons(int head, Node tail) -> new Cons(head, copy(tail));
        };
    }
}
