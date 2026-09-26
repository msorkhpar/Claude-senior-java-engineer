package practice;

import java.util.List;

public class SealedTree {

    /** Every leaf kind of the sealed type root, in permits order; a non-sealed one ends with +. */
    public static List<String> leaves(Class<?> root) {
        throw new UnsupportedOperationException("write leaves");
    }
}
