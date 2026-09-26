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
        return hasSealedAncestor(type) ? "non-sealed" : "open";
    }

    private static boolean hasSealedAncestor(Class<?> type) {
        Class<?> parent = type.getSuperclass();
        if (parent != null && (parent.isSealed() || hasSealedAncestor(parent))) {
            return true;
        }
        for (Class<?> direct : type.getInterfaces()) {
            if (direct.isSealed() || hasSealedAncestor(direct)) {
                return true;
            }
        }
        return false;
    }
}
