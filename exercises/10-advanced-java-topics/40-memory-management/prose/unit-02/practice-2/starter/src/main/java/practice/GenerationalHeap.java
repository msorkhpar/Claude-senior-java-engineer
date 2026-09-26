package practice;

import java.util.List;
import java.util.Set;

public final class GenerationalHeap {

    public enum Generation { EDEN, SURVIVOR, OLD }

    public GenerationalHeap(int tenuringThreshold) {
    }

    /** A new object in Eden, age 0. */
    public void allocate(String id) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Collects the young generation; returns the freed ids in allocation order. */
    public List<String> minorGc(Set<String> reachable) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Collects every generation; returns the freed ids in allocation order. */
    public List<String> majorGc(Set<String> reachable) {
        throw new UnsupportedOperationException("TODO");
    }

    public Generation generation(String id) {
        throw new UnsupportedOperationException("TODO");
    }

    public int age(String id) {
        throw new UnsupportedOperationException("TODO");
    }
}
