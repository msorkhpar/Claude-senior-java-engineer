package practice;

import java.util.List;
import java.util.ListIterator;

public final class Echo {

    private Echo() {
    }

    /** Inserts element + suffix right after each element of the caller's list. */
    public static void expand(List<String> list, String suffix) {
        ListIterator<String> it = list.listIterator();
        while (it.hasNext()) {
            String current = it.next();
            it.add(current + suffix);
        }
    }
}
