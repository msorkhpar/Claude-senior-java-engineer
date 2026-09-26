package practice;

import java.util.Collection;

/** A set that counts every element anyone tried to add. Built by composition. */
public class CountingSet<E> {

    public boolean add(E e) {
        throw new UnsupportedOperationException("write add");
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("write addAll");
    }

    public int getAddCount() {
        throw new UnsupportedOperationException("write getAddCount");
    }

    public boolean contains(Object o) {
        throw new UnsupportedOperationException("write contains");
    }

    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
