package practice;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** A set that counts every element anyone tried to add. Built by composition. */
public final class CountingSet<E> {
    private final Set<E> items = new HashSet<>();
    private int addCount;

    public boolean add(E e) {
        addCount++;
        return items.add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return items.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }

    public boolean contains(Object o) {
        return items.contains(o);
    }

    public int size() {
        return items.size();
    }
}
