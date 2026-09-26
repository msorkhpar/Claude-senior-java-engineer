package practice;

import java.util.List;

public final class Lifecycle {

    private Lifecycle() {
    }

    /** Whether one thread could have entered these states, in this order. */
    public static boolean isValid(List<Thread.State> states) {
        throw new UnsupportedOperationException("write isValid");
    }
}
