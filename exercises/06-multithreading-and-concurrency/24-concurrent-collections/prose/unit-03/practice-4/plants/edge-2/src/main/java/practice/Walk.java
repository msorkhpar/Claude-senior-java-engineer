package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Walk {

    private Walk() {
    }

    /** Calls onVisit on each element the list held when the walk began; returns them in order. */
    public static List<String> visitAll(CopyOnWriteArrayList<String> list, Consumer<String> onVisit) {
        List<String> visited = new ArrayList<>();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            String s = list.get(i);
            visited.add(s);
            onVisit.accept(s);
        }
        return visited;
    }
}
