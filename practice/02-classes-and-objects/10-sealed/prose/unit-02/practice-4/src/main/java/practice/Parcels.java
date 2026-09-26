package practice;

public class Parcels {

    public sealed interface Parcel permits Letter, Box, Tube {
    }

    public record Letter(int grams) implements Parcel {
    }

    public record Box(int kilograms) implements Parcel {
    }

    public record Tube(int centimetres) implements Parcel {
    }

    public static int rate(Parcel p) {
        throw new UnsupportedOperationException("write rate");
    }
}
