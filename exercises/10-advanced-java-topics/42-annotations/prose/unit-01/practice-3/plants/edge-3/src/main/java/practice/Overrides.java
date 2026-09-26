package practice;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;

public final class Overrides {

    private Overrides() {
    }

    /** Whether m overrides or implements a method of a supertype of its declaring class. */
    public static boolean isOverride(Method m) {
        if (!overridable(m)) {
            return false;
        }
        Deque<Class<?>> todo = new ArrayDeque<>();
        Class<?> owner = m.getDeclaringClass();
        if (owner.getSuperclass() != null) {
            todo.add(owner.getSuperclass());
        }
        while (!todo.isEmpty()) {
            Class<?> type = todo.poll();
            try {
                Method candidate = type.getDeclaredMethod(m.getName(), m.getParameterTypes());
                if (overridable(candidate)) {
                    return true;
                }
            } catch (NoSuchMethodException ignored) {
                // not declared here: keep climbing
            }
            if (type.getSuperclass() != null) {
                todo.add(type.getSuperclass());
            }
        }
        return false;
    }

    private static boolean overridable(Method m) {
        int mod = m.getModifiers();
        return !Modifier.isStatic(mod) && !Modifier.isPrivate(mod);
    }
}
