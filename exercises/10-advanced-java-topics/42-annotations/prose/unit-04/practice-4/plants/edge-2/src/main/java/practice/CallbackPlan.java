package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CallbackPlan {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface OnStart {
        int order() default 0;
    }

    private final List<Method> callbacks;

    private CallbackPlan(List<Method> callbacks) {
        this.callbacks = callbacks;
    }

    /** Reads and checks type's @OnStart methods. */
    public static CallbackPlan of(Class<?> type) {
        List<Method> found = new ArrayList<>();
        List<String> bad = new ArrayList<>();
        for (Method m : type.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(OnStart.class)) {
                continue;
            }
            if (m.getParameterCount() != 0) {
                throw new IllegalArgumentException(
                        type.getSimpleName() + ": @OnStart methods must take no parameters: " + m.getName());
            }
            found.add(m);
        }
        if (!bad.isEmpty()) {
            throw new IllegalArgumentException(
                    type.getSimpleName() + ": @OnStart methods must take no parameters: " + String.join(", ", bad));
        }
        found.sort(Comparator.comparingInt(m -> m.getAnnotation(OnStart.class).order()));
        for (Method m : found) {
            m.setAccessible(true);
        }
        return new CallbackPlan(List.copyOf(found));
    }

    /** The callbacks' names in run order. */
    public List<String> names() {
        return callbacks.stream().map(Method::getName).toList();
    }

    /** Calls the callbacks on bean in run order. */
    public void run(Object bean) throws IllegalAccessException, InvocationTargetException {
        for (Method m : callbacks) {
            m.invoke(bean);
        }
    }
}
