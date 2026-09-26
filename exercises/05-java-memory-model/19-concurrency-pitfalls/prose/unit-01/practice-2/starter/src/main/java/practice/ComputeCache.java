package practice;

import java.util.function.Function;

public final class ComputeCache {

    public ComputeCache(Function<String, String> compute) {
    }

    /** Returns the key's value, computing it at most once per key. */
    public String get(String key) {
        throw new UnsupportedOperationException("write get");
    }

    /** Returns how many keys are cached. */
    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
