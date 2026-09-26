package practice;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Range implements Iterable<Integer> {

    private final int start;
    private final int end;

    public Range(int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end " + end + " is before start " + start);
        }
        this.start = start;
        this.end = end;
    }

    /** The factory method: a new iterator over start (inclusive) to end (exclusive). */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int next = start;

            @Override
            public boolean hasNext() {
                return next < end;
            }

            @Override
            public Integer next() {
                if (next >= end) {
                    throw new NoSuchElementException();
                }
                return next++;
            }
        };
    }
}
