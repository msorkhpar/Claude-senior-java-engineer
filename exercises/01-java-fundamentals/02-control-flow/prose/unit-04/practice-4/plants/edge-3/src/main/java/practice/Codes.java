package practice;

public final class Codes {

    private Codes() {
    }

    /** Returns the fruit a code names, "unknown" for any other text, and "missing" for null. */
    public static String name(String code) {
        if (code == null) {
            return "missing";
        }
        if (code == "AP") {
            return "apple";
        }
        if (code == "BN") {
            return "banana";
        }
        if (code == "CH") {
            return "cherry";
        }
        return "unknown";
    }
}
