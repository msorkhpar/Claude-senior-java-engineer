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
        return switch (p) {
            case Letter l when l.grams() <= 20 -> 1;
            case Letter l -> 2;
            case Box b -> 10 + b.kilograms();
            case Tube t -> 8;
        };
    }
}
