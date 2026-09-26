package practice;

import java.util.Enumeration;
import java.util.Iterator;

public final class Legacy {

    private Legacy() {
    }

    /** Adapts a legacy Enumeration to Iterator. */
    public static <T> Iterator<T> asIterator(Enumeration<T> enumeration) {
        throw new UnsupportedOperationException("write asIterator");
    }

    /** Adapts an Iterator to the legacy Enumeration. */
    public static <T> Enumeration<T> asEnumeration(Iterator<T> iterator) {
        throw new UnsupportedOperationException("write asEnumeration");
    }
}
