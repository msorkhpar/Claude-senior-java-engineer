package practice;

import java.util.Collection;
import java.util.HashSet;

/** A set that counts every element anyone tried to add. Built by inheritance. */
public class CountingSet<E> extends HashSet<E> {
    private int addCount;

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
