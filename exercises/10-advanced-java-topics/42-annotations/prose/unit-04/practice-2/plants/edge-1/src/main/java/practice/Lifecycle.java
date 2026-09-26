package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

public final class Lifecycle {

    private Lifecycle() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PostConstruct {
        int order() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PreDestroy {
        int order() default 0;
    }

    /** Calls target's @PostConstruct methods, lowest order first. */
    public static void initialize(Object target) throws Exception {
        run(target, PostConstruct.class, PostConstruct::order);
    }

    /** Calls target's @PreDestroy methods, lowest order first. */
    public static void destroy(Object target) throws Exception {
        run(target, PreDestroy.class, PreDestroy::order);
    }

    private static <A extends Annotation> void run(Object target, Class<A> type, ToIntFunction<A> order)
            throws Exception {
        List<Method> methods = new ArrayList<>();
        for (Method m : target.getClass().getMethods()) {
            if (m.isAnnotationPresent(type)) {
                methods.add(m);
            }
        }
        methods.sort(Comparator.comparingInt(m -> order.applyAsInt(m.getAnnotation(type))));
        for (Method m : methods) {
            m.setAccessible(true);
            try {
                m.invoke(target);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception cause) {
                    throw cause;
                }
                throw (Error) e.getCause();
            }
        }
    }
}
