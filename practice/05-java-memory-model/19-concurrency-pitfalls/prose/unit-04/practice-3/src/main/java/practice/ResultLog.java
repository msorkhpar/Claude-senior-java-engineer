package practice;

import java.util.List;
import java.util.function.UnaryOperator;

public final class ResultLog {

    /** Transforms the input outside the lock, then appends the result under the lock and returns it. */
    public String processAndStore(String input, UnaryOperator<String> transform) {
        throw new UnsupportedOperationException("write processAndStore");
    }

    /** The stored results, in order, as a copy the caller owns. */
    public List<String> results() {
        throw new UnsupportedOperationException("write results");
    }
}
