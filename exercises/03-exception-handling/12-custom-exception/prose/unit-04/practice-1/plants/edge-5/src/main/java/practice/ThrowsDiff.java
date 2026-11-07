package practice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares one public method of the SDK between the last release and the next, and lists what the
 * next release's throws clause would break in a customer's build. CI fails the release when the
 * list for any method is not empty.
 */
public final class ThrowsDiff {

    private ThrowsDiff() {
    }

    /**
     * The exception types in {@code next}'s throws clause that a customer's code, written against
     * {@code last}, would no longer compile without. In the order {@code next} declares them.
     */
    public static List<Class<?>> breaking(Method last, Method next) {
        List<Class<?>> found = new ArrayList<>();
        for (Class<?> type : next.getExceptionTypes()) {
            if (isChecked(type) && !handledBy(type, last.getExceptionTypes())) {
                found.add(type);
            }
        }
        return found;
    }

    /** Whether the compiler makes a caller catch or declare an exception of this type. */
    static boolean isChecked(Class<?> type) {
        return !RuntimeException.class.isAssignableFrom(type) && !Error.class.isAssignableFrom(type);
    }

    /**
     * Whether code that already compiled against a method declaring {@code declared} also handles
     * an exception of this type.
     */
    static boolean handledBy(Class<?> type, Class<?>[] declared) {
        for (Class<?> d : declared) {
            if (d.isAssignableFrom(type) || type.isAssignableFrom(d)) {
                return true;
            }
        }
        return false;
    }
}
