package practice;

import java.util.List;
import java.util.ListIterator;

public final class Echo {

    private Echo() {
    }

    /** Inserts element + suffix right after each element of the caller's list. */
    public static void expand(List<String> list, String suffix) {
        for (int i = 0; i < list.size(); i++) {
            String current = list.get(i);
            if (!current.endsWith(suffix)) {
                list.add(i + 1, current + suffix);
            }
        }
    }
}
