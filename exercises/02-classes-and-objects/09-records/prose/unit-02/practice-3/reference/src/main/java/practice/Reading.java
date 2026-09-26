package practice;

import java.util.Arrays;
import java.util.Objects;

public record Reading(String sensor, double[] values) {

    /** Keep a copy of the array. */
    public Reading {
        values = values.clone();
    }

    /** A copy, so the caller cannot change this reading. */
    @Override
    public double[] values() {
        return values.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Reading that
                && Objects.equals(sensor, that.sensor)
                && Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(sensor) + Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return "Reading[sensor=" + sensor + ", values=" + Arrays.toString(values) + "]";
    }
}
