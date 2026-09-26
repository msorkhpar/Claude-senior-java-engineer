package practice;

public final class Labels {

    private Labels() {
    }

    /** Labels any object. */
    public static String label(Object obj) {
        return "object: " + obj;
    }

    /** Labels a String by its length, or says there is none. */
    public static String label(String text) {
        return text == null ? "no text" : "text of " + text.length();
    }
}
