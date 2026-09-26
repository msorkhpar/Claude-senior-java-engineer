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
            if (Modifier.isAbstract(m.getModifiers()) && !isObjectMethod(m)) {
                abstractMethods++;
            }
        }
        return abstractMethods == 1;
    }

    private static boolean isObjectMethod(Method m) {
        for (Method o : Object.class.getMethods()) {
            if (o.getName().equals(m.getName())) {
                return true;
            }
        }
        return false;
    }
}
