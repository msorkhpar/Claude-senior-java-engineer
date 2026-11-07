package practice;

import java.util.ArrayList;
import java.util.List;

/** The nightly temperature report of the plant: one line per event from the sensors. */
public class SensorReport {

    /**
     * A temperature reading. A sensor that has not been calibrated yet sends NaN, and the record
     * refuses to hand that out; it also refuses a value no thermometer can read.
     */
    public record Reading(String sensor, double celsius) {
        @Override
        public double celsius() {
            if (Double.isNaN(celsius)) throw new IllegalStateException("sensor " + sensor + " is not calibrated");
            if (celsius < -273.15) throw new IllegalArgumentException("below absolute zero: " + celsius);
            return celsius;
        }
    }

    /** A sensor that stopped sending for a while. */
    public record Outage(String sensor, int minutes) {}

    /** The report: one line per event, in order. */
    public static List<String> report(List<?> events) {
        List<String> lines = new ArrayList<>();
        for (Object event : events) lines.add(safeLine(event));
        return lines;
    }

    /** The line for one event. An event of any other kind is a bug upstream. */
    static String line(Object event) {
        if (event instanceof Reading(var sensor, var celsius)) return sensor + " " + celsius + " C";
        if (event instanceof Outage(var sensor, var minutes)) return sensor + " offline " + minutes + " min";
        throw new IllegalArgumentException("unknown event: " + event);
    }

    /** The line for one event, as the report prints it. */
    static String safeLine(Object event) {
        try {
            return line(event);
        } catch (MatchException e) {
            if (e.getCause() instanceof IllegalStateException && event instanceof Reading r) {
                return r.sensor() + " uncalibrated";
            }
            throw e;
        }
    }
}
