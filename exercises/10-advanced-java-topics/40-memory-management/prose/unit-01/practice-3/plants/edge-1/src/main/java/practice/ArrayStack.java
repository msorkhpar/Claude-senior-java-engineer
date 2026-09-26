package practice;

import java.util.Arrays;
import java.util.EmptyStackException;

public final class ArrayStack<E> {

    private Object[] elements;
    private int size;

    public ArrayStack(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1: " + capacity);
        }
        this.elements = new Object[capacity];
    }

    public void push(E element) {
        if (size == elements.length) {
            elements = Arrays.copyOf(elements, 2 * elements.length);
        }
        elements[size++] = element;
    }

    /** Removes and returns the top element. */
    @SuppressWarnings("unchecked")
    public E pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return (E) elements[--size];
    }

    /** Returns the top element without removing it. */
    @SuppressWarnings("unchecked")
    public E peek() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return (E) elements[size - 1];
    }

    public int size() {
        return size;
    }
}
