package practice;

public class LinkedStack<T> {

    private static class Node<T> {
        private final T value;
        private final Node<T> next;

        private Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public void push(T value) {
        top = new Node<>(value, top);
        size++;
    }

    public T pop() {
        T value = peek();
        if (top == null) {
            return null;
        }
        top = top.next;
        size--;
        return value;
    }

    public T peek() {
        return top == null ? null : top.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
