package practice;

import java.lang.reflect.Array;

public final class ArrayMaker {

    private ArrayMaker() {
    }

    /** A new array of type with every element set to value. */
    public static Object filled(Class<?> type, int length, Object value) {
        Object array = Array.newInstance(type, length);
        for (int i = 0; i < length; i++) {
            if (value != null) {
                Array.set(array, i, value);
            }
        }
        return array;
    }

    /** A new array of the same component type holding the elements that fit. */
    public static Object resize(Object array, int newLength) {
        Object copy = Array.newInstance(array.getClass().getComponentType(), newLength);
        int kept = Math.min(Array.getLength(array), newLength);
        for (int i = 0; i < kept; i++) {
            Array.set(copy, i, Array.get(array, i));
        }
        return copy;
    }

    /** A rows x cols array of type whose every row exists. */
    public static Object grid(Class<?> type, int rows, int cols) {
        return Array.newInstance(type, rows, cols);
    }
}
