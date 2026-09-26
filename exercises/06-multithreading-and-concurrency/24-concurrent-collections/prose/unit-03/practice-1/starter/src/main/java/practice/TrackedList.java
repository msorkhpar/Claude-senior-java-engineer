package practice;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public final class TrackedList implements Iterable<String> {

    private String[] items = new String[4];
    private int size;

    public void add(String s) {
        if (size == items.length) {
            items = Arrays.copyOf(items, size * 2);
        }
        items[size++] = s;
    }

    public String removeAt(int index) {
        Objects.checkIndex(index, size);
        String old = items[index];
        System.arraycopy(items, index + 1, items, index, size - index - 1);
        items[--size] = null;
        return old;
    }

    public void set(int index, String s) {
        Objects.checkIndex(index, size);
        items[index] = s;
    }

    public String get(int index) {
        Objects.checkIndex(index, size);
        return items[index];
    }

    public int size() {
        return size;
    }

    /** A fail-fast iterator over the list. */
    @Override
    public Iterator<String> iterator() {
        throw new UnsupportedOperationException("write iterator");
    }
}
