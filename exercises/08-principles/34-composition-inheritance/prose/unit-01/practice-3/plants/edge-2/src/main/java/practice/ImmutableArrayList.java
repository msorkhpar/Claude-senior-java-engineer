package practice;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

public class ImmutableArrayList<E> extends AbstractList<E> {
    private final List<? extends E> source;

    public ImmutableArrayList(Collection<? extends E> source) {
        this.source = (List<? extends E>) source;
    }

    @Override
    public E get(int index) {
        return source.get(index);
    }

    @Override
    public int size() {
        return source.size();
    }
}
