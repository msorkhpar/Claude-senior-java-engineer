package practice;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** A small growable list whose iterator is fail-fast. The list part is written; write iterator(). */
public class TinyList<E> implements Iterable<E> {

    private Object[] items = new Object[4];
    private int size;
    /** Moves on every structural change: add and removeAt. */
    private int modCount;

    public void add(E item) {
        if (size == items.length) {
            items = Arrays.copyOf(items, size * 2);
        }
        items[size++] = item;
        modCount++;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        Objects.checkIndex(index, size);
        return (E) items[index];
    }

    public int size() {
        return size;
    }

    public void removeAt(int index) {
        Objects.checkIndex(index, size);
        System.arraycopy(items, index + 1, items, index, size - index - 1);
        items[--size] = null;
        modCount++;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int cursor;
            private int lastReturned = -1;
            private int expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public E next() {
                checkForComodification();
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                lastReturned = cursor++;
                return get(lastReturned);
            }

            @Override
            public void remove() {
                checkForComodification();
                removeAt(cursor - 1);
                cursor = cursor - 1;
                lastReturned = -1;
                expectedModCount = modCount;
            }

            private void checkForComodification() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
            }
        };
    }
}
