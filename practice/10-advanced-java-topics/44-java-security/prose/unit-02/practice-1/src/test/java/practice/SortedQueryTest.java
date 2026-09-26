package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortedQueryTest {

    private static final String BASE = "SELECT id, name, email FROM users ORDER BY ";

    private static String typed(String text) {
        return new StringBuilder(text).toString();
    }

    @Test
    void sortsByAnAllowedColumn() {
        assertThat(SortedQuery.sql(typed("name"), typed("asc"))).isEqualTo(BASE + "name ASC");
        assertThat(SortedQuery.sql(typed("created_at"), typed("DESC"))).isEqualTo(BASE + "created_at DESC");
        assertThat(SortedQuery.sql(typed("email"), typed("Desc"))).isEqualTo(BASE + "email DESC");
        assertThatThrownBy(() -> SortedQuery.sql("password", "asc")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aColumnMustMatchANameExactly() {
        assertThatThrownBy(() -> SortedQuery.sql("name; DROP TABLE users", "asc"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SortedQuery.sql("name, (SELECT password FROM users)", "asc"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SortedQuery.sql("x_name", "asc"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SortedQuery.sql("Name", "asc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theDirectionIsAllowListedToo() {
        assertThatThrownBy(() -> SortedQuery.sql("email", "desc, (SELECT 1)"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SortedQuery.sql("email", "descending"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SortedQuery.sql("email", "asc "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SortedQuery.sql("email", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
