package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Walk {

    private Walk() {
    }

    /** Calls onVisit on each element the list held when the walk began; returns them in order. */
    public static List<String> visitAll(CopyOnWriteArrayList<String> list, Consumer<String> onVisit) {
        throw new UnsupportedOperationException("write visitAll");
    }
}
