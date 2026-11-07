package practice;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the greenhouse dashboard from one batch of sensor readings. The batch comes from a
 * message queue: a sensor can report several times in one batch, and a reading that was retried
 * can arrive after a newer one.
 */
public class Readings {

    /** One measurement. {@code unit} is null while the sensor hasn't been calibrated. */
    public record Reading(String sensor, double value, String unit, long takenAt) {}

    /** One dashboard line per sensor, in sensor order: its latest value and, if known, its unit. */
    public static List<String> dashboard(List<Reading> batch) {
        Map<String, String> units = unitBySensor(batch);
        return new TreeMap<>(latestBySensor(batch)).values().stream()
                .map(r -> r.sensor() + ": " + r.value() + " " + units.getOrDefault(r.sensor(), "(uncalibrated)"))
                .toList();
    }

    /** The most recent reading of each sensor in the batch, by the time it was taken. */
    static Map<String, Reading> latestBySensor(List<Reading> batch) {
        return batch.stream().collect(Collectors.toMap(Reading::sensor, Function.identity(),
                BinaryOperator.maxBy(Comparator.comparingLong(Reading::takenAt))));
    }

    /** The unit each sensor reports in. A sensor that has no unit yet has no entry. */
    static Map<String, String> unitBySensor(List<Reading> batch) {
        return batch.stream()
                .filter(r -> r.unit() != null)
                .collect(Collectors.toMap(Reading::sensor, Reading::unit, (first, again) -> first));
    }
}
