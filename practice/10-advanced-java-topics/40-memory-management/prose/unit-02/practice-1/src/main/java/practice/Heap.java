package practice;

import java.util.List;

public final class Heap {

    /** Adds an object; a name already in use is refused. */
    public void allocate(String name) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Makes object 'from' refer to object 'to'. */
    public void reference(String from, String to) {
        throw new UnsupportedOperationException("TODO");
    }

    public void addRoot(String name) {
        throw new UnsupportedOperationException("TODO");
    }

    public void removeRoot(String name) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Marks from the roots, sweeps, and returns the freed names in allocation order. */
    public List<String> collect() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Names of the objects still allocated, in allocation order. */
    public List<String> live() {
        throw new UnsupportedOperationException("TODO");
    }
}
