package practice;

public final class Codes {

    private Codes() {
    }

    /** Returns the fruit a code names, "unknown" for any other text, and "missing" for null. */
    public static String name(String code) {
        if (code == null) {
            return "missing";
        }
        String name;
        switch (code) {
            case "AP":
                name = "apple";
                break;
            case "BN":
                name = "banana";
                break;
            case "CH":
                name = "cherry";
                break;
            default:
                name = "unknown";
        }
        return name;
    }
}
