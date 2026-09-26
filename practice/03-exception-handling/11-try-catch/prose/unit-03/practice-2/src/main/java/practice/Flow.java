package practice;

import java.util.List;
import java.util.function.IntSupplier;

public final class Flow {

    private Flow() {
    }

    /** Evaluates {@code body}, recording "try", "catch <message>" and "finally" in {@code log}. */
    public static int run(IntSupplier body, List<String> log) {
        throw new UnsupportedOperationException("write run");
    }
}
