package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The platform team's interceptor registry: services register an action for an annotation
 * type, and every call made through the registry to a method carrying that annotation runs the
 * action first.
 */
public class Interceptors {

    private final Map<Class<? extends Annotation>, Consumer<String>> actions = new LinkedHashMap<>();

    /**
     * Registers an action to run before every method that carries the annotation. Refuses, with
     * IllegalArgumentException, an annotation type the registry could never find on a method.
     */
    public void register(Class<? extends Annotation> type, Consumer<String> action) {
        RetentionPolicy policy = policyOf(type);
        if (!visibleToReflection(policy)) {
            throw new IllegalArgumentException("@" + type.getSimpleName() + " has retention " + policy
                    + ", so no method will ever be found carrying it");
        }
        actions.put(type, action);
    }

    /** Calls the no-argument method on the target, after the actions for its annotations. */
    public Object call(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        for (Map.Entry<Class<? extends Annotation>, Consumer<String>> e : actions.entrySet()) {
            if (m.isAnnotationPresent(e.getKey())) e.getValue().accept(methodName);
        }
        m.setAccessible(true);
        return m.invoke(target);
    }

    /** The retention policy that applies to annotations of this type. */
    static RetentionPolicy policyOf(Class<? extends Annotation> type) {
        Retention retention = type.getAnnotation(Retention.class);
        return retention == null ? RetentionPolicy.CLASS : retention.value();
    }

    /** Whether annotations kept under this policy can be found on a method while the program runs. */
    static boolean visibleToReflection(RetentionPolicy policy) {
        return policy == RetentionPolicy.RUNTIME;
    }
}
