package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class LazyLog {

    private final BooleanSupplier enabled;
    private final List<String> lines = new ArrayList<>();

    /** Creates a log that records messages only while {@code enabled} answers true. */
    public LazyLog(BooleanSupplier enabled) {
        this.enabled = enabled;
    }

    /** Records {@code message.get()} if the switch is on right now; otherwise leaves the Supplier untouched. */
    public void debug(Supplier<String> message) {
        String text = message.get();
        if (enabled.getAsBoolean()) {
            lines.add(text);
        }
    }

    /** Returns the recorded messages, oldest first. */
    public List<String> lines() {
        return List.copyOf(lines);
    }
}
