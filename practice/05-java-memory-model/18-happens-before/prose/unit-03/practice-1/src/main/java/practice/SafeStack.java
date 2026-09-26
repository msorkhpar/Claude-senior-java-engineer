package practice;

public final class SafeStack<T> {

    /** Adds an item on top. */
    public void push(T item) {
        throw new UnsupportedOperationException("write push");
    }

    /** Removes and returns the top item, or throws EmptyStackException when empty. */
    public T pop() {
        throw new UnsupportedOperationException("write pop");
    }

    /** Returns how many items the stack holds. */
    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
