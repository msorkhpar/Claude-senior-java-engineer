package practice;

public final class Classifier {

    private Classifier() {
    }

    /** Classifies a number by its type and value. */
    public static String classify(Object obj) {
        return switch (obj) {
            case Integer i when i < 0 -> "Negative integer";
            case Integer i when i == 0 -> "Zero";
            case Integer i when i <= 100 -> "Small positive integer";
            case Integer i -> "Large positive integer";
            case Double d -> "Finite double";
            default -> "Not a number type";
        };
    }
}
