package practice;

public final class TypeKinds {

    private TypeKinds() {
    }

    /** "primitive", "array", "annotation", "interface", "enum", "record" or "class". */
    public static String kind(Class<?> type) {
        if (type.isPrimitive()) {
            return "primitive";
        }
        if (type.isArray()) {
            return "array";
        }
        if (type.isInterface()) {
            return "interface";
        }
        if (type.isAnnotation()) {
            return "annotation";
        }
        if (type.isEnum()) {
            return "enum";
        }
        if (type.isRecord()) {
            return "record";
        }
        return "class";
    }

    /** The innermost element type of an array type, or null for any other type. */
    public static Class<?> elementType(Class<?> type) {
        if (!type.isArray()) {
            return null;
        }
        Class<?> element = type.getComponentType();
        while (element.isArray()) {
            element = element.getComponentType();
        }
        return element;
    }
}
