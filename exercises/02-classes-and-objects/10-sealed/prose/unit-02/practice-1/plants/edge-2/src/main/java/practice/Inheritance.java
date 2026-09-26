package practice;

import java.util.Arrays;

public class Inheritance {

    public static String verdict(Class<?> parent, Class<?> candidate) {
        if (parent.isSealed() && !Arrays.asList(parent.getPermittedSubclasses()).contains(candidate)) {
            return "class is not allowed to extend sealed class: " + parent.getSimpleName();
        }
        return null;
    }
}
