package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public final class CallbackPlan {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface OnStart {
        int order() default 0;
    }

    private CallbackPlan() {
    }

    /** Reads and checks type's @OnStart methods. */
    public static CallbackPlan of(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The callbacks' names in run order. */
    public List<String> names() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Calls the callbacks on bean in run order. */
    public void run(Object bean) throws IllegalAccessException, InvocationTargetException {
        throw new UnsupportedOperationException("TODO");
    }
}
