package practice;

import java.util.ArrayList;
import java.util.List;

public final class SelectBuilder {

    private String table;
    private final List<String> columns = new ArrayList<>();
    private final List<String> conditions = new ArrayList<>();
    private String orderBy;

    public SelectBuilder from(String table) {
        this.table = table;
        return this;
    }

    public SelectBuilder columns(String... names) {
        columns.addAll(List.of(names));
        return this;
    }

    public SelectBuilder where(String condition) {
        conditions.add(condition);
        return this;
    }

    public SelectBuilder orderBy(String column) {
        this.orderBy = column;
        return this;
    }

    public String build() {
        if (table == null || table.isBlank()) {
            throw new IllegalStateException("a table is required");
        }
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(columns.isEmpty() ? "*" : String.join(", ", columns))
                .append(" FROM ").append(table);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (orderBy != null) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        return sql.toString();
    }
}
