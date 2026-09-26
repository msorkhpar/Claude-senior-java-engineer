package practice;

import java.util.Collection;
import java.util.Set;

public class CountingSet<E> {
    private final Set<E> inner;
    private int addCount;

    public CountingSet(Set<E> inner) {
        this.inner = new java.util.HashSet<>(inner);
    }

    public boolean add(E e) {
        addCount++;
        return inner.add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return inner.addAll(c);
    }

    public boolean contains(Object o) {
        return inner.contains(o);
    }

    public int size() {
        return inner.size();
    }

    public int addCount() {
        return addCount;
    }
}
