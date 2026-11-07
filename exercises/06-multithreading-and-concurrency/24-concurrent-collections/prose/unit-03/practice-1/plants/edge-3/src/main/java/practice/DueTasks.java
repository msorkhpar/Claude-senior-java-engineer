package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/** The ids of the tasks due on this tick of the scheduler, in the order they were scheduled. */
public class DueTasks implements Iterable<String> {

    private String[] ids = new String[4];
    private int size;

    /** Counts structural changes: every add and every removal. A set() is not one. */
    int modCount;

    public void add(String id) {
        if (size == ids.length) ids = Arrays.copyOf(ids, size * 2);
        ids[size++] = id;
        modCount++;
    }

    public String removeAt(int index) {
        Objects.checkIndex(index, size);
        String old = ids[index];
        System.arraycopy(ids, index + 1, ids, index, size - index - 1);
        ids[--size] = null;
        modCount++;
        return old;
    }

    public void set(int index, String id) {
        Objects.checkIndex(index, size);
        ids[index] = id;
    }

    public String get(int index) {
        Objects.checkIndex(index, size);
        return ids[index];
    }

    public int size() {
        return size;
    }

    public List<String> toList() {
        return new ArrayList<>(Arrays.asList(ids).subList(0, size));
    }

    @Override
    public Iterator<String> iterator() {
        return new Walk();
    }

    /** One walk over the list. */
    class Walk implements Iterator<String> {
        int cursor;                         // index of the next id to return
        int lastReturned = -1;              // index of the id next() returned last, -1 if none
        int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        /**
         * Returns the next id. If the list was changed structurally since this walk started, other
         * than through this walk's own remove(), it throws ConcurrentModificationException instead.
         * Past the end it throws NoSuchElementException.
         */
        @Override
        public String next() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (cursor >= size) throw new NoSuchElementException();
            lastReturned = cursor;
            return ids[cursor++];
        }

        /**
         * Removes the id the last next() returned. The walk carries on with the id that followed it,
         * and without throwing.
         */
        @Override
        public void remove() {
            if (lastReturned < 0) throw new IllegalStateException("next() first");
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            removeAt(lastReturned);
            cursor = lastReturned;
            lastReturned = -1;
        }
    }
}
