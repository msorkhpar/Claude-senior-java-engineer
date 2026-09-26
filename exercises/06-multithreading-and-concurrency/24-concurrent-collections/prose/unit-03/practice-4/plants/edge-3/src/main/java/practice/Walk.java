package practice;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
public final class Walk {
    private Walk() {}
    private static final List<String> VISITED = new ArrayList<>();
    public static List<String> visitAll(CopyOnWriteArrayList<String> list, Consumer<String> onVisit) {
        VISITED.clear();
        for (String s : list) { VISITED.add(s); onVisit.accept(s); }
        return VISITED;
    }
}
