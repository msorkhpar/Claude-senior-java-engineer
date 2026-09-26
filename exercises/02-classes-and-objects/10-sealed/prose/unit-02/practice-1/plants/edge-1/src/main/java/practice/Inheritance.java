package practice;

import java.lang.reflect.Modifier;

public class Inheritance {

    public static String verdict(Class<?> parent, Class<?> candidate) {
        if (Modifier.isFinal(parent.getModifiers())) {
            return "cannot inherit from final " + parent.getSimpleName();
        }
        return null;
    }
}
