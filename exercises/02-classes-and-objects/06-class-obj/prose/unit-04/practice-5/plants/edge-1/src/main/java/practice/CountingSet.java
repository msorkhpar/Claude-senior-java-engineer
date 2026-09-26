package practice;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CountingSet<E> extends HashSet<E> {
    private int addCount;

    public CountingSet(Set<E> inner) {
        super(inner);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int addCount() {
        return addCount;
    }
}
