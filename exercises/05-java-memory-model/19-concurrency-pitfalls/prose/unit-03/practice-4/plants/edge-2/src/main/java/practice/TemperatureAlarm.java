package practice;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public final class TemperatureAlarm {

    /** Where readings come from; it may call the listener during register, and from any thread. */
    public interface EventSource {
        void register(IntConsumer listener);
    }

    private int threshold;
    private final AtomicInteger alarms = new AtomicInteger();

    private TemperatureAlarm(int threshold) {
        this.threshold = threshold;
    }

    /** Returns an alarm registered with the source. */
    public static TemperatureAlarm create(int threshold, EventSource source) {
        TemperatureAlarm alarm = new TemperatureAlarm(threshold);
        source.register(alarm::onReading);
        return alarm;
    }

    private void onReading(int celsius) {
        if (celsius > threshold) {
            alarms.incrementAndGet();
        }
    }

    public int threshold() {
        return threshold;
    }

    /** How many readings were strictly above the threshold. */
    public int alarms() {
        return alarms.get();
    }
}
