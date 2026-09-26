package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectBuilderTest {

    @Test
    void buildsAFullQuery() {
        SelectBuilder builder = new SelectBuilder()
                .from("users").columns("id", "name").where("age > ?").where("active = ?").orderBy("name");
        String sql = builder.build();

        assertThat(sql).isEqualTo("SELECT id, name FROM users WHERE age > ? AND active = ? ORDER BY name");
        assertThat(builder.build()).isEqualTo(sql);
    }

    @Test
    void stepsMayComeInAnyOrder() {
        String sql = new SelectBuilder()
                .orderBy("id").where("age > ?").columns("id").from("users").where("active = ?")
                .build();

        assertThat(sql).isEqualTo("SELECT id FROM users WHERE age > ? AND active = ? ORDER BY id");
    }

    @Test
    void noConditionMeansNoWhere() {
        assertThat(new SelectBuilder().from("users").build()).isEqualTo("SELECT * FROM users");
        assertThat(new SelectBuilder().from("users").orderBy("id").build()).isEqualTo("SELECT * FROM users ORDER BY id");
    }

    @Test
    void aTableIsRequired() {
        assertThatThrownBy(() -> new SelectBuilder().columns("id").build()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new SelectBuilder().from("  ").build()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void conditionsKeepTheOrderGiven() {
        String sql = new SelectBuilder().from("orders")
                .where("status = ?").where("total > ?").where("region = ?").where("created_at < ?").where("id <> ?")
                .build();

        assertThat(sql).isEqualTo(
                "SELECT * FROM orders WHERE status = ? AND total > ? AND region = ? AND created_at < ? AND id <> ?");
    }
}
