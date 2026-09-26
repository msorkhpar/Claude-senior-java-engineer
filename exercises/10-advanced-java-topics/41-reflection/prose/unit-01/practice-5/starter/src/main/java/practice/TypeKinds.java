package practice;

public final class TypeKinds {

    private TypeKinds() {
    }

    /** "primitive", "array", "annotation", "interface", "enum", "record" or "class". */
    public static String kind(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The innermost element type of an array type, or null for any other type. */
    public static Class<?> elementType(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
