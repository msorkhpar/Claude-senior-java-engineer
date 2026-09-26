package practice;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Objects;

/** A fixed list, built on AbstractList: a superclass designed and documented for extension. */
public class ImmutableArrayList<E> extends AbstractList<E> {
    private final Object[] elements;

    public ImmutableArrayList(Collection<? extends E> source) {
        this.elements = source.toArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        Objects.checkIndex(index, elements.length);
        return (E) elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }
}
