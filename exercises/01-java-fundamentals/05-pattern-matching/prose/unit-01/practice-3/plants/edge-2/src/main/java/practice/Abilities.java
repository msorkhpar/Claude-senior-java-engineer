package practice;

import java.util.ArrayList;
import java.util.List;

public final class Abilities {

    private Abilities() {
    }

    /** Names the interfaces among Runnable, Comparable and CharSequence that obj implements. */
    public static List<String> of(Object obj) {
        List<String> found = new ArrayList<>();
        for (Class<?> type : obj.getClass().getInterfaces()) {
            if (type == Runnable.class || type == Comparable.class || type == CharSequence.class) {
                found.add(type.getSimpleName());
            }
        }
        found.sort(java.util.Comparator.comparing(name -> List.of("Runnable", "Comparable", "CharSequence").indexOf(name)));
        return found;
    }
}
