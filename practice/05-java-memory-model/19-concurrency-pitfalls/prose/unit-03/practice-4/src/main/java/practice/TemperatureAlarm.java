package practice;

import java.util.function.IntConsumer;

public final class TemperatureAlarm {

    /** Where readings come from; it may call the listener during register, and from any thread. */
    public interface EventSource {
        void register(IntConsumer listener);
    }

    /** Returns an alarm registered with the source. */
    public static TemperatureAlarm create(int threshold, EventSource source) {
        throw new UnsupportedOperationException("write create");
    }

    public int threshold() {
        throw new UnsupportedOperationException("write threshold");
    }

    /** How many readings were strictly above the threshold. */
    public int alarms() {
        throw new UnsupportedOperationException("write alarms");
    }
}
