package practice;

public final class Greeting {

    private Greeting() {
    }

    /** Joins title and name with a space, leaving out a null part. */
    public static String address(String title, String name) {
        if (title == null) {
            return name;
        }
        return title.concat(" ").concat(name);
    }
}
