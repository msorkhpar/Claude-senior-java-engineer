package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

public final class Binder {

    private Binder() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface RequestParam {
        String name();

        boolean required() default true;
    }

    /** The arguments for m, bound from the query by each parameter's @RequestParam. */
    public static Object[] bind(Method m, Map<String, String> query) {
        Annotation[][] annotations = m.getParameterAnnotations();
        Class<?>[] types = m.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            RequestParam param = find(annotations[i]);
            if (param == null) {
                throw new IllegalStateException("parameter " + i + " of " + m.getName() + " has no @RequestParam");
            }
            String raw = query.get(param.name());
            if (raw == null) {
                if (param.required()) {
                    throw new IllegalArgumentException("missing required parameter " + param.name());
                }
                args[i] = types[i] == int.class ? (Object) 0 : null;
            } else {
                args[i] = types[i] == int.class ? (Object) Integer.parseInt(raw) : raw;
            }
        }
        return args;
    }

    private static RequestParam find(Annotation[] onParameter) {
        for (Annotation a : onParameter) {
            if (a instanceof RequestParam param) {
                return param;
            }
        }
        return null;
    }
}
