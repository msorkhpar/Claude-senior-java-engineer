package practice;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;

public final class Legacy {

    private Legacy() {
    }

    /** Adapts a legacy Enumeration to Iterator. */
    public static <T> Iterator<T> asIterator(Enumeration<T> enumeration) {
        Objects.requireNonNull(enumeration, "enumeration must not be null");
        return Collections.list(enumeration).iterator();
    }

    /** Adapts an Iterator to the legacy Enumeration. */
    public static <T> Enumeration<T> asEnumeration(Iterator<T> iterator) {
        Objects.requireNonNull(iterator, "iterator must not be null");
        return new Enumeration<>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public T nextElement() {
                return iterator.next();
            }
        };
    }
}
