package practice;

import java.lang.reflect.Modifier;

public class SealedKinds {

    public static String kindOf(Class<?> type) {
        if (Modifier.isFinal(type.getModifiers())) {
            return "final";
        }
        if (type.isSealed()) {
            return "sealed";
        }
        if (type.getSuperclass() != null && type.getSuperclass().isSealed()) {
            return "non-sealed";
        }
        return "open";
    }
}
