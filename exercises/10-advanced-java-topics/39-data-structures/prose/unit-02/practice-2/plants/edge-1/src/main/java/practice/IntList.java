package practice;

import java.util.Arrays;

public final class IntList {

    private int[] values;
    private int size;
    private long copies;

    public IntList(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1: " + initialCapacity);
        }
        this.values = new int[initialCapacity];
    }

    /** Appends a value, growing the array when it is full. */
    public void add(int value) {
        if (size == values.length) {
            int grown = values.length + Math.min(16, Math.max(1, values.length / 2));
            values = Arrays.copyOf(values, grown);
            copies += size;
        }
        values[size++] = value;
    }

    /** The value at index; only indices below size() are allowed. */
    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " for size " + size);
        }
        return values[index];
    }

    public int size() {
        return size;
    }

    /** Length of the backing array. */
    public int capacity() {
        return values.length;
    }

    /** Elements copied by all growth steps so far. */
    public long copies() {
        return copies;
    }
}
