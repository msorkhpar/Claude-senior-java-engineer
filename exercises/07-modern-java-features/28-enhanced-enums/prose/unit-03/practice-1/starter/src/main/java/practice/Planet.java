package practice;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6),
    MARS(6.421e+23, 3.3972e6),
    JUPITER(1.9e+27, 7.1492e7),
    SATURN(5.688e+26, 6.0268e7),
    URANUS(8.686e+25, 2.5559e7),
    NEPTUNE(1.024e+26, 2.4746e7);

    static final double G = 6.67300E-11;

    private final double mass;    // kilograms
    private final double radius;  // meters

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    public double mass() {
        return mass;
    }

    public double radius() {
        return radius;
    }

    public double surfaceGravity() {
        throw new UnsupportedOperationException("write surfaceGravity");
    }

    public double surfaceWeight(double otherMass) {
        throw new UnsupportedOperationException("write surfaceWeight");
    }

    /** The planet with the highest surface gravity among {@code among}. */
    public static Planet strongest(Collection<Planet> among) {
        throw new UnsupportedOperationException("write strongest");
    }

    /** Every planet whose surface gravity is within [min, max], in declaration order. */
    public static List<Planet> withGravityBetween(double min, double max) {
        throw new UnsupportedOperationException("write withGravityBetween");
    }
}
