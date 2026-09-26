package practice;

import java.util.List;
import java.util.function.IntSupplier;

public final class Flow {

    private Flow() {
    }

    /** Evaluates {@code body}, recording "try", "catch <message>" and "finally" in {@code log}. */
    public static int run(IntSupplier body, List<String> log) {
        try {
            log.add("try");
            return body.getAsInt();
        } catch (IllegalStateException e) {
            log.add("catch " + e.getMessage());
            log.add("finally");
            return -1;
        }
    }
}
