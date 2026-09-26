package practice;

public final class Kinds {

    private Kinds() {
    }

    /** Names the kind of obj, most specific type first. */
    public static String kind(Object obj) {
        if (obj instanceof Integer) {
            return "integer";
        } else if (obj instanceof Number) {
            return "number";
        } else if (obj instanceof CharSequence) {
            return "text";
        } else if (obj instanceof int[]) {
            return "int array";
        } else if (obj instanceof Object[]) {
            return "object array";
        }
        return "other";
    }
}
