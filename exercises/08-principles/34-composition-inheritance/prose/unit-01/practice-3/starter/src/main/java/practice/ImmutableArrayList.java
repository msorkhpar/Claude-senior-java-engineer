package practice;

import java.util.AbstractList;
import java.util.Collection;

/** A fixed list, built on AbstractList: a superclass designed and documented for extension. */
public class ImmutableArrayList<E> extends AbstractList<E> {

    public ImmutableArrayList(Collection<? extends E> source) {
        throw new UnsupportedOperationException("write the constructor");
    }

    @Override
    public E get(int index) {
        throw new UnsupportedOperationException("write get");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
