package practice;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class LazyLog {

    /** Creates a log that records messages only while {@code enabled} answers true. */
    public LazyLog(BooleanSupplier enabled) {
        throw new UnsupportedOperationException("write LazyLog");
    }

    /** Records {@code message.get()} if the switch is on right now; otherwise leaves the Supplier untouched. */
    public void debug(Supplier<String> message) {
        throw new UnsupportedOperationException("write debug");
    }

    /** Returns the recorded messages, oldest first. */
    public List<String> lines() {
        throw new UnsupportedOperationException("write lines");
    }
}
