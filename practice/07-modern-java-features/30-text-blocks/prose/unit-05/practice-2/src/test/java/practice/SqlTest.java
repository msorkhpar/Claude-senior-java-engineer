package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlTest {

    private static String query(String... lines) {
        return String.join("\n", lines);
    }

    @Test
    void buildsTheQueryWithAPlaceholder() {
        assertThat(Sql.select(new String("users"), List.of("name", "email"), new String("id")))
                .isEqualTo(query("SELECT name, email", "FROM users", "WHERE id = ?"));
        assertThat(Sql.select(new String("order_items"), List.of("sku"), new String("_order_id2")))
                .isEqualTo(query("SELECT sku", "FROM order_items", "WHERE _order_id2 = ?"));
    }

    @Test
    void noColumnsSelectsEverything() {
        assertThat(Sql.select(new String("orders"), List.of(), new String("status")))
                .isEqualTo(query("SELECT *", "FROM orders", "WHERE status = ?"));
    }

    @Test
    void anInjectedTableNameIsRefused() {
        assertThatThrownBy(() -> Sql.select("users; DROP TABLE users", List.of("name"), "id"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.select("users", List.of("name"), "id = 1 OR 1"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.select("users\n", List.of("name"), "id")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.select("1users", List.of("name"), "id")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.select("user$", List.of("name"), "id")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anInjectedColumnNameIsRefused() {
        assertThatThrownBy(() -> Sql.select("users", List.of("name", "1=1 --"), "id"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Sql.select("users", List.of("password FROM admins --"), "id"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
