package practice;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/** Forwards every Set method to the set it wraps. */
class ForwardingSet<E> implements Set<E> {
    private final Set<E> delegate;

    ForwardingSet(Set<E> delegate) {
        this.delegate = delegate; // TODO: refuse a null set
    }

    @Override public int size() { return delegate.size(); }
    @Override public boolean isEmpty() { return delegate.isEmpty(); }
    @Override public boolean contains(Object o) { return delegate.contains(o); }
    @Override public Iterator<E> iterator() { return delegate.iterator(); }
    @Override public Object[] toArray() { return delegate.toArray(); }
    @Override public <T> T[] toArray(T[] a) { return delegate.toArray(a); }
    @Override public boolean add(E e) { return delegate.add(e); }
    @Override public boolean remove(Object o) { return delegate.remove(o); }
    @Override public boolean containsAll(Collection<?> c) { return delegate.containsAll(c); }
    @Override public boolean addAll(Collection<? extends E> c) { return delegate.addAll(c); }
    @Override public boolean retainAll(Collection<?> c) { return delegate.retainAll(c); }
    @Override public boolean removeAll(Collection<?> c) { return delegate.removeAll(c); }
    @Override public void clear() { delegate.clear(); }

    // TODO: equals, hashCode and toString
}

/** A set that counts every element anyone tried to add, by wrapping another set. */
public class InstrumentedSet<E> extends ForwardingSet<E> {

    public InstrumentedSet(Set<E> delegate) {
        super(delegate);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("write add");
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("write addAll");
    }

    /** How many elements were offered through add and addAll, duplicates included. */
    public int getAddCount() {
        throw new UnsupportedOperationException("write getAddCount");
    }
}
