package practice;

import java.util.Arrays;
import java.util.EmptyStackException;

public final class SafeStack<T> {

    private Object[] elements = new Object[16];
    private int size;

    /** Adds an item on top. */
    public synchronized void push(T item) {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, size * 2);
        }
        elements[size++] = item;
    }

    /** Removes and returns the top item, or throws EmptyStackException when empty. */
    @SuppressWarnings("unchecked")
    public synchronized T pop() {
        T top = (T) elements[--size];
        elements[size] = null;
        return top;
    }

    /** Returns how many items the stack holds. */
    public synchronized int size() {           // reads take the lock too
        return size;
    }
}
