package practice;

import java.util.ArrayList;
import java.util.List;

public class SealedTree {

    public static List<String> leaves(Class<?> root) {
        if (!root.isSealed()) {
            throw new IllegalArgumentException(root.getSimpleName() + " is not sealed");
        }
        List<String> found = new ArrayList<>();
        collect(root, found);
        return found;
    }

    private static void collect(Class<?> sealed, List<String> found) {
        for (Class<?> permitted : sealed.getPermittedSubclasses()) {
            if (permitted.isSealed()) {
                collect(permitted, found);
            } else {
                found.add(permitted.getSimpleName());
            }
        }
    }
}
