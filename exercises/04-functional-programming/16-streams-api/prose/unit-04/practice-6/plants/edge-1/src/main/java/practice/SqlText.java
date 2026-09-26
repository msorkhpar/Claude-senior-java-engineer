package practice;

import java.util.List;
import java.util.stream.Collectors;

public final class SqlText {

    private SqlText() {
    }

    /** The ids in list order, separated by ", ", inside parentheses. */
    public static String inClause(List<Integer> ids) {
        StringBuilder sql = new StringBuilder("(");
        ids.forEach(id -> sql.append(id).append(", "));
        sql.setLength(sql.length() - 2);
        return sql.append(")").toString();
    }

    /** The non-blank names in list order, separated by ", ", inside square brackets. */
    public static String nameList(List<String> names) {
        return names.stream().filter(n -> !n.isBlank()).collect(Collectors.joining(", ", "[", "]"));
    }
}
