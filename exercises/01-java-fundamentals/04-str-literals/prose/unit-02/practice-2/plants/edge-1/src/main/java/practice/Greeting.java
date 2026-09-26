package practice;

public final class Greeting {

    private Greeting() {
    }

    /** Joins title and name with a space, leaving out a null part. */
    public static String address(String title, String name) {
        if (name == null) {
            return title == null ? "" : title;
        }
        return title + " " + name;
    }
}
