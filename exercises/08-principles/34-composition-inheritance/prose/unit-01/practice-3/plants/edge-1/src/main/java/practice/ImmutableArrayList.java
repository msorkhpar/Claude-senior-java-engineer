package practice;

import java.util.ArrayList;
import java.util.Collection;

/** Reuses ArrayList by extending it: a concrete class that was not designed for this. */
public class ImmutableArrayList<E> extends ArrayList<E> {

    public ImmutableArrayList(Collection<? extends E> source) {
        super(source);
    }
}
