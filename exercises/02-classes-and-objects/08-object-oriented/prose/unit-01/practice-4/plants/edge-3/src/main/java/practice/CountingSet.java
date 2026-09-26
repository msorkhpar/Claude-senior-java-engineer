package practice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A set that counts every element anyone tried to add. Built by composition. */
public final class CountingSet<E> {
    private final List<E> items = new ArrayList<>();
    private int addCount;

    public boolean add(E e) {
        addCount++;
        if (contains(e)) {
            return false;
        }
        items.add(e);
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            changed |= add(e);
        }
        return changed;
    }

    public int getAddCount() {
        return addCount;
    }

    public boolean contains(Object o) {
        for (E e : items) {
            if (e == o) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return items.size();
    }
}
