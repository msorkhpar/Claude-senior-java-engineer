package practice;

import java.util.ArrayList;
import java.util.List;

public final class SelectBuilder {

    private String table;
    private final List<String> columns = new ArrayList<>();
    private final StringBuilder tail = new StringBuilder();
    private boolean anyCondition;

    public SelectBuilder from(String table) {
        this.table = table;
        return this;
    }

    public SelectBuilder columns(String... names) {
        columns.addAll(List.of(names));
        return this;
    }

    public SelectBuilder where(String condition) {
        tail.append(anyCondition ? " AND " : " WHERE ").append(condition);
        anyCondition = true;
        return this;
    }

    public SelectBuilder orderBy(String column) {
        tail.append(" ORDER BY ").append(column);
        return this;
    }

    public String build() {
        if (table == null || table.isBlank()) {
            throw new IllegalStateException("a table is required");
        }
        return "SELECT " + (columns.isEmpty() ? "*" : String.join(", ", columns)) + " FROM " + table + tail;
    }
}
