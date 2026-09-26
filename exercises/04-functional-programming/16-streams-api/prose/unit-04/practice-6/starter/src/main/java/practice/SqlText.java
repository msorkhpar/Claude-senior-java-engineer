package practice;

import java.util.List;

public final class SqlText {

    private SqlText() {
    }

    /** The ids in list order, separated by ", ", inside parentheses. */
    public static String inClause(List<Integer> ids) {
        throw new UnsupportedOperationException("write inClause");
    }

    /** The non-blank names in list order, separated by ", ", inside square brackets. */
    public static String nameList(List<String> names) {
        throw new UnsupportedOperationException("write nameList");
    }
}
