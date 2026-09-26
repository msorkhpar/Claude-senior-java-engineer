package practice;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SealedTree {

    public static List<String> leaves(Class<?> root) {
        List<String> found = new ArrayList<>();
        Class<?>[] permitted = root.getPermittedSubclasses();
        if (permitted == null) {
            return found;
        }
        for (Class<?> kind : permitted) {
            if (kind.isSealed()) {
                found.addAll(leaves(kind));
            } else if (Modifier.isFinal(kind.getModifiers())) {
                found.add(kind.getSimpleName());
            } else {
                found.add(kind.getSimpleName() + "+");
            }
        }
        return found;
    }
}
