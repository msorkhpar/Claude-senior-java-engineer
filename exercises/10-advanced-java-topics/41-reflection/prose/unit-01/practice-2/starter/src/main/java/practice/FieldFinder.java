package practice;

import java.util.List;

public final class FieldFinder {

    private FieldFinder() {
    }

    /** The names of the fields this type declares itself, any access level, sorted. */
    public static List<String> ownFields(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The names of every public field, inherited ones included, sorted. */
    public static List<String> publicFields(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The names of every field of this type and its superclasses: own first, each class sorted. */
    public static List<String> allFields(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
