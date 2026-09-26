package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlQueryTest {

    private static String query(String table, String where) {
        return String.join("\n", "SELECT *", "FROM " + table, where, "ORDER BY id;");
    }

    @Test
    void buildsTheFourLines() {
        assertThat(SqlQuery.of(new String("users"), new String("name"), new String("Alice")))
                .isEqualTo(query("users", "WHERE name = 'Alice'"));
        assertThat(SqlQuery.of(new String("orders"), new String("status"), new String("active")))
                .isEqualTo(query("orders", "WHERE status = 'active'"));
        assertThat(SqlQuery.of(new String("t"), new String("c"), new String(""))).isEqualTo(query("t", "WHERE c = ''"));
    }

    @Test
    void aQuoteInTheValueIsDoubled() {
        assertThat(SqlQuery.of(new String("users"), new String("name"), new String("O'Brien")))
                .isEqualTo(query("users", "WHERE name = 'O''Brien'"));
        assertThat(SqlQuery.of(new String("t"), new String("c"), new String("''")))
                .isEqualTo(query("t", "WHERE c = ''''''"));
    }

    @Test
    void aBackslashInTheValueIsLeftAlone() {
        assertThat(SqlQuery.of(new String("files"), new String("path"), new String("C:\\temp")))
                .isEqualTo(query("files", "WHERE path = 'C:" + '\\' + "temp'"));
    }
}
