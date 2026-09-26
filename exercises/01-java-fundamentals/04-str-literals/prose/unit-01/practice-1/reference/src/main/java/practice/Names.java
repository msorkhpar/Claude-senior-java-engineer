package practice;

public final class Names {

    private Names() {
    }

    /** Returns the pooled String with the same text, or null for null. */
    public static String canonical(String name) {
        return name == null ? null : name.intern();
    }
}
