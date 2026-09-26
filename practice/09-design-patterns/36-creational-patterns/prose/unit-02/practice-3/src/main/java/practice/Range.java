package practice;

import java.util.Iterator;

public final class Range implements Iterable<Integer> {

    public Range(int start, int end) {
    }

    /** The factory method: a new iterator over start (inclusive) to end (exclusive). */
    @Override
    public Iterator<Integer> iterator() {
        throw new UnsupportedOperationException("write iterator");
    }
}
