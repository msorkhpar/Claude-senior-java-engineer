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
        throw new UnsupportedOperationException("write iterator");
    }
}
