package practice;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class TrackedList implements Iterable<String> {

    private String[] items = new String[4];
    private int size;
    private int modCount;

    public void add(String s) {
        if (size == items.length) {
            items = Arrays.copyOf(items, size * 2);
        }
        items[size++] = s;
        modCount++;
    }

    public String removeAt(int index) {
        Objects.checkIndex(index, size);
        String old = items[index];
        System.arraycopy(items, index + 1, items, index, size - index - 1);
        items[--size] = null;
        modCount++;
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
        return new Iterator<>() {
            private int cursor;
            private int lastReturned = -1;
            private boolean removed;
            private int expectedModCount = modCount;

            private void check() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public String next() {
                check();
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                lastReturned = cursor;
                removed = false;
                return items[cursor++];
            }

            @Override
            public void remove() {
                if (removed) {
                    throw new IllegalStateException("next() first");
                }
                check();
                removeAt(cursor - 1);
                cursor--;
                removed = true;
                expectedModCount = modCount;
            }
        };
    }
}
