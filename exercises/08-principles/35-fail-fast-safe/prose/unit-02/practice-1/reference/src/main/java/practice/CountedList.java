package practice;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** A list that counts its structural changes; its iterator is fail-fast on that count. */
public class CountedList<E> implements Iterable<E> {

    private final List<E> items = new ArrayList<>();
    private int modCount;

    public int modCount() {
        return modCount;
    }

    public int size() {
        return items.size();
    }

    public E get(int index) {
        return items.get(index);
    }

    public void add(E item) {
        items.add(item);
        modCount++;
    }

    public E remove(int index) {
        E removed = items.remove(index);
        modCount++;
        return removed;
    }

    public E set(int index, E item) {
        return items.set(index, item);
    }

    public void clear() {
        items.clear();
        modCount++;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int cursor;
            private final int expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                return cursor < items.size();
            }

            @Override
            public E next() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (cursor >= items.size()) {
                    throw new NoSuchElementException();
                }
                return items.get(cursor++);
            }
        };
    }
}
