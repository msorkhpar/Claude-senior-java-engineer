package practice;

import java.util.AbstractList;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * A {@code List<Integer>} over the {@code int[]} buffer the sensor library fills.
 * The list has the buffer's size: add and remove throw UnsupportedOperationException.
 */
public final class IntArrayList extends AbstractList<Integer> implements RandomAccess {

    private final int[] buffer;

    /** The list presents this buffer. */
    public IntArrayList(int[] buffer) {
        // TODO: keep what the list will present; refuse null.
        throw new IllegalStateException("TODO: IntArrayList(int[])");
    }

    /** The value at {@code index}. */
    @Override
    public Integer get(int index) {
        // TODO
        throw new IllegalStateException("TODO: get");
    }

    /** Puts {@code value} at {@code index}, as List.set does. */
    @Override
    public Integer set(int index, Integer value) {
        // TODO: honour List.set's whole contract.
        throw new IllegalStateException("TODO: set");
    }

    /** How many values the list holds. */
    @Override
    public int size() {
        // TODO
        throw new IllegalStateException("TODO: size");
    }
}
