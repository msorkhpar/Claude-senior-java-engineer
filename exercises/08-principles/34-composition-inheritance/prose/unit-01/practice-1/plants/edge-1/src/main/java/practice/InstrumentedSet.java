package practice;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class InstrumentedSet<E> extends HashSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> delegate) {
        super();
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

    public int getAddCount() {
        return addCount;
    }
}
