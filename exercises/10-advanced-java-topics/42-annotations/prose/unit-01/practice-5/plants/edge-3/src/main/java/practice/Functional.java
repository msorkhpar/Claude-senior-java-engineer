package practice;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class Functional {

    private Functional() {
    }

    /** Whether type is an interface with exactly one abstract method, as the compiler counts. */
    public static boolean isFunctional(Class<?> type) {
        if (!type.isInterface()) {
            return false;
        }
        int abstractMethods = 0;
        for (Method m : type.getMethods()) {
            if (!m.isDefault() && !isObjectMethod(m)) {
                abstractMethods++;
            }
        }
        return abstractMethods == 1;
    }

    private static boolean isObjectMethod(Method m) {
        try {
            Object.class.getMethod(m.getName(), m.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
