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
        for (Class<?> i : owner.getInterfaces()) {
            todo.add(i);
        }
        while (!todo.isEmpty()) {
            Class<?> type = todo.poll();
            for (Method candidate : type.getDeclaredMethods()) {
                if (candidate.getName().equals(m.getName()) && overridable(candidate)) {
                    return true;
                }
            }
            if (type.getSuperclass() != null) {
                todo.add(type.getSuperclass());
            }
            for (Class<?> i : type.getInterfaces()) {
                todo.add(i);
            }
        }
        return false;
    }

    private static boolean overridable(Method m) {
        int mod = m.getModifiers();
        return !Modifier.isStatic(mod) && !Modifier.isPrivate(mod);
    }
}
