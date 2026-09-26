package practice;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public class Inheritance {

    public static String verdict(Class<?> parent, Class<?> candidate) {
        if (Modifier.isFinal(parent.getModifiers())) {
            return "cannot inherit from final " + parent.getSimpleName();
        }
        for (Class<?> type = parent; type != null; type = type.getSuperclass()) {
            if (type.isSealed() && !Arrays.asList(type.getPermittedSubclasses()).contains(candidate)) {
                return "class is not allowed to extend sealed class: " + type.getSimpleName();
            }
        }
        return null;
    }
}
