package practice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Invoker {

    private Invoker() {
    }

    /** Invokes the method with these exact parameter types on target. */
    public static Object call(Object target, String name, Class<?>[] types, Object... args)
            throws ReflectiveOperationException {
        Method method = target.getClass().getDeclaredMethod(name, types);
        method.setAccessible(true);
        return invoke(method, target, args);
    }

    /** Invokes the static method with these exact parameter types on type. */
    public static Object callStatic(Class<?> type, String name, Class<?>[] types, Object... args)
            throws ReflectiveOperationException {
        Method method = type.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return invoke(method, null, args);
    }

    private static Object invoke(Method method, Object target, Object[] args) throws IllegalAccessException {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }
}
