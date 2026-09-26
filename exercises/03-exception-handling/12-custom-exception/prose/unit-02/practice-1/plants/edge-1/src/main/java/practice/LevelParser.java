package practice;

public final class LevelParser {

    private LevelParser() {
    }

    /** "low" is 1, "high" is 3; anything else throws IllegalArgumentException("Unknown level: " + s). */
    public static int parseLevel(String s) {
        return switch (s) {
            case "low" -> 1;
            case "high" -> 3;
            default -> throw new IllegalArgumentException("Unknown level");
        };
    }
}
