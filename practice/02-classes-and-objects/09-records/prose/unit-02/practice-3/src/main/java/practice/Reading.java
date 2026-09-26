package practice;

import java.util.Arrays;
import java.util.Objects;

public record Reading(String sensor, double[] values) {

    /** Keep a copy of the array. */
    public Reading {
        throw new UnsupportedOperationException("write the compact constructor");
    }

    /** A copy, so the caller cannot change this reading. */
    @Override
    public double[] values() {
        throw new UnsupportedOperationException("write values");
    }

    @Override
    public boolean equals(Object other) {
        throw new UnsupportedOperationException("write equals");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("write hashCode");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("write toString");
    }
}
