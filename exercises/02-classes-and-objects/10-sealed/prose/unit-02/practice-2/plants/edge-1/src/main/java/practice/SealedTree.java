package practice;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SealedTree {

    public static List<String> leaves(Class<?> root) {
        if (!root.isSealed()) {
            throw new IllegalArgumentException(root.getSimpleName() + " is not sealed");
        }
        List<String> found = new ArrayList<>();
        for (Class<?> permitted : root.getPermittedSubclasses()) {
            boolean open = !permitted.isSealed() && !Modifier.isFinal(permitted.getModifiers());
            found.add(permitted.getSimpleName() + (open ? "+" : ""));
        }
        return found;
    }
}
