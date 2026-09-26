package practice;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class LazyLog {

    private final BooleanSupplier enabled;
    private final List<String> lines = new ArrayList<>();

    public LazyLog(BooleanSupplier enabled) {
        this.enabled = enabled;
    }

    public void debug(Supplier<String> message) {
        if (enabled.getAsBoolean() && message.get() != null) {
            lines.add(message.get());
        }
    }

    public List<String> lines() {
        return List.copyOf(lines);
    }
}
