package practice;

import java.util.List;
import java.util.stream.Collectors;

public final class SqlText {

    private SqlText() {
    }

    public static String inClause(List<Integer> ids) {
        return ids.stream().distinct().map(String::valueOf).collect(Collectors.joining(", ", "(", ")"));
    }

    public static String nameList(List<String> names) {
        return names.stream().filter(n -> !n.isBlank()).collect(Collectors.joining(", ", "[", "]"));
    }
}
